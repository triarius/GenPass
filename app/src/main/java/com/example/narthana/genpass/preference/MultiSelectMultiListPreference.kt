package com.example.narthana.genpass.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.preference.DialogPreference
import android.preference.Preference
import android.util.AttributeSet
import android.util.SparseArray
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.narthana.genpass.R

/**
 * Created by narthana on 6/01/17.
 */

class MultiSelectMultiListPreference @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
        defStyleRes: Int = defStyleAttr): DialogPreference(context, attrs) {
    private val mEntries: Array<CharSequence>
    private val mEntryValues: Array<CharSequence>
    private val mColumnEntries: Array<CharSequence>
    private val mColEntryValues: Array<CharSequence>
    private val mColumnDependencies: SparseArray<Set<Int>>

    private val numRows: Int
    private val numCols: Int

    private var mCheckBoxes: Array<Array<CheckBox>>? = null
    private var mValues: Map<String, MutableSet<String>>? = null
    private var mSelectedValues: Map<String, MutableSet<String>>? = null

    init {
        val styledAttrs = context.theme.obtainStyledAttributes(attrs, R.styleable.MultiSelectMultiListPreference, 0, 0)
        mEntries = styledAttrs.getTextArray(R.styleable.MultiSelectMultiListPreference_android_entries)
        mEntryValues = styledAttrs.getTextArray(R.styleable.MultiSelectMultiListPreference_android_entryValues)
        mColumnEntries = styledAttrs.getTextArray(R.styleable.MultiSelectMultiListPreference_columnEntries)
        mColEntryValues = styledAttrs.getTextArray(R.styleable.MultiSelectMultiListPreference_columnEntryValues)
        val columnDepsId = styledAttrs.getResourceId(R.styleable.MultiSelectMultiListPreference_columnDependencies, -1)
        styledAttrs.recycle()

        numRows = mEntries.size
        numCols = mColumnEntries.size

        // gather the data on dependencies between columns
        val colDeps = context.resources.obtainTypedArray(columnDepsId)
        mColumnDependencies = SparseArray<Set<Int>>(colDeps.length())
        (0 .. colDeps.length() - 1)
                .map { context.resources.getStringArray(colDeps.getResourceId(it, -1)) }
                .map { it.map { mColEntryValues.indexOf(it) } }
                .forEach { mColumnDependencies.put(it[0], it.slice(IntRange(1, it.lastIndex)).toSet()) }
        colDeps.recycle()
    }

    override fun onCreateDialogView(): View {
        // create a table with a grid of checkboxes
        val table = TableLayout(context)
        table.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )

        // the 0th column should stretch to fill the dialog
        table.setColumnStretchable(0, true)
        run {
            val padding = Utility.dpToPx(context, DIALOG_PADDING)
            table.setPaddingRelative(padding, padding, padding, padding)
        }

        // create header row
        val header = TableRow(context)

        // params for the first row
        val fistRowParams = TableLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        )
        table.addView(header, fistRowParams)

        // create the column headings and put them in the header row
        val headingPadding = Utility.dpToPx(context, HEADING_PADDING)
        val headingParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        )
        headingParams.gravity = Gravity.CENTER_HORIZONTAL
        for (j in 0 .. numCols - 1) {
            val headingTextView = TextView(context)
            headingTextView.text = mColumnEntries[j]
            headingTextView.setPadding(headingPadding, headingPadding, headingPadding, headingPadding)
            headingParams.column = j
            header.addView(headingTextView, headingParams)
        }

        // create the rest of rows
        val rowParams = TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val entryParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        )
        entryParams.gravity = Gravity.CENTER_VERTICAL
        entryParams.column = 0
        // create the check box 2d array
        mCheckBoxes = Array<Array<CheckBox>>(numRows){Array<CheckBox>(numCols){CheckBox(context)}}
        for (i in mCheckBoxes!!.indices) {
            val row = TableRow(context)
            // create the row label and add it to the row
            val entryTextView = TextView(context)
            entryTextView.text = mEntries[i]
            row.addView(entryTextView, entryParams)
            // create the checkboxes
            for (j in mCheckBoxes!![i].indices)
            {
                val checkBox = mCheckBoxes!![i][j]
                // Set the check change listener
                val dependents = mColumnDependencies.get(j)
                checkBox.setOnCheckedChangeListener(
                        if (dependents != null) IndependentCheckChangeListener(i, j, dependents)
                        else                    DependentCheckChangeListener(i, j)
                )
                val checkBoxParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                )
                checkBoxParams.gravity = Gravity.CENTER_HORIZONTAL
                checkBoxParams.column = j
                row.addView(checkBox, checkBoxParams)
            }
            table.addView(row, rowParams)
        }

        return table
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        updateCheckStates(mSelectedValues!!)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) persistValues(mSelectedValues)
        else mSelectedValues = cloneValues(mValues!!)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        persistValues(
                if (restorePersistedValue) getValuesFromResources(mValues)
                else defaultValue as Map<String, MutableSet<String>>
        )
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        val array = context.resources.obtainTypedArray(a.getResourceId(index, -1))
        val values = (0 .. numCols - 1).associate { j ->
            Pair(colValue(j), array.getTextArray(j).map { it.toString() }.toHashSet())
        }
        array.recycle()
        return values
    }

    private fun getValuesFromResources(defaultValue: Map<String, MutableSet<String>>?):
            Map<String, MutableSet<String>> = (0 .. numCols - 1).associate {
        // Note: do not remove the cloning of v. It is necessary for
        // new data to be written to the preferences. Android expects the output of
        // Preference.getStringSet to not be modified. Thus when it receives it back
        // in onDialogClosed, it just keeps the old data we got here
        // source: stackoverflow.com/questions/12528836/shared-preferences-only-saved-first-time
        // source: developer.android.com/reference/android/content/SharedPreferences.html
        //     "Objects that are returned from the various get methods must be treated as
        //      immutable by the application."
        val k = colValue(it)
        val v = sharedPreferences.getStringSet(key + k, defaultValue?.get(k)).toHashSet()
        Pair(k, v)
    }

    private fun persistValues(values: Map<String, MutableSet<String>>?) {
        if (values == null) return
        mValues = values
        mSelectedValues = cloneValues(values)
        if (shouldPersist()) {
            MSMLSavePreference.save(editor, key, values)
//            editor.putBoolean(key, true)
//            values.forEach { (k, v) -> editor.putStringSet(key + k, v.toHashSet()) }
//            editor.apply()
        }
    }

    private fun updateCheckStates(values: Map<String, Set<String>>) {
        if (mCheckBoxes != null) {
            for (i in mCheckBoxes!!.indices) for (j in mCheckBoxes!![i].indices)
                mCheckBoxes!![i][j].isChecked =
                        values[colValue(j)]!!.contains(mEntryValues[i].toString())
            for (k in 0..mColumnDependencies.size() - 1) {
                val independentColNo = mColumnDependencies.keyAt(k)
                val dependents = mColumnDependencies.get(independentColNo)
                for (d in dependents) for (i in mCheckBoxes!!.indices)
                    mCheckBoxes!![i][d].isEnabled = mCheckBoxes!![i][independentColNo].isChecked
            }
        }
    }

    private fun colValue(j: Int): String = j.toString()

    private fun <K, T> cloneValues(values: Map<K, MutableSet<T>>): Map<K, MutableSet<T>>
            = values.entries.associate { Pair(it.key, it.value) }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

//        // Check whether this Preference is persistent (continually saved)
//        // No need to save instance state since it's persistent,use superclass state
//        if (isPersistent) return superState;

        // Create instance of custom BaseSavedState
        val myState = SavedState(superState)
        // Set the state's value with the class member that holds current setting value
        myState.values = mSelectedValues!!
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || state.javaClass != SavedState::class.java) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state)
            return
        }

        // Cast state to custom BaseSavedState and pass to superclass
        val myState = state as SavedState
        super.onRestoreInstanceState(myState.getSuperState())

        // Set this Preference's widget to reflect the restored state
        mSelectedValues = myState.values
        updateCheckStates(mSelectedValues!!)
    }

    private class SavedState: Preference.BaseSavedState {
        // Member that holds the setting's value
        internal var values: Map<String, MutableSet<String>>? = null

        internal constructor(superState: Parcelable): super(superState) {}

        internal constructor(source: Parcel): super(source) {
            val size = source.readInt()
            val keys = arrayOfNulls<String>(size)
            source.readStringArray(keys)
            values = keys.associate {
                val n = source.readInt()
                val value = arrayOfNulls<String>(n)
                source.readStringArray(value)
                it!! to value.map{it!!}.toHashSet()
            }
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            val keysArray = values!!.keys.toTypedArray()
            dest.writeInt(keysArray.size)
            dest.writeStringArray(keysArray)
            for (v in values!!.values) {
                dest.writeInt(v.size)
                dest.writeStringArray(v.toTypedArray())
            }
        }

        companion object {
            // Standard creator object using an instance of this class
            val CREATOR: Parcelable.Creator<SavedState> = object: Parcelable.Creator<SavedState> {
                override fun createFromParcel(inParcel: Parcel) = SavedState(inParcel)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    private open inner class DependentCheckChangeListener internal constructor(
            internal val mI: Int, internal val mJ: Int): CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            val rowEntry = mEntryValues[mI].toString()
            val colEntry = colValue(mJ)
            if (isChecked) mSelectedValues!![colEntry]!!.add(rowEntry)
            else mSelectedValues!![colEntry]!!.remove(rowEntry)
        }
    }

    private inner class IndependentCheckChangeListener internal constructor(
            i: Int, j: Int, private val mDependents: Set<Int>): DependentCheckChangeListener(i, j) {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            super.onCheckedChanged(buttonView, isChecked)
            for (d in mDependents) {
                mCheckBoxes!![mI][d].isEnabled = isChecked
                mCheckBoxes!![mI][d].isChecked = false
            }
        }
    }

    companion object {
        val DIALOG_PADDING = 14.0f
        val HEADING_PADDING = 3.0f
    }
}


