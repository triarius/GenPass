package com.example.android.genpass.preference

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
import com.example.android.genpass.R

/**
 * Created by narthana on 6/01/17.
 */

class MultiSelectMultiListPreference(context: Context, attrs: AttributeSet):
        DialogPreference(context, attrs) {
    private val mEntries: List<String>
    private val mEntryValues: List<String>
    private val mColumnEntries: List<String>
    private val mColEntryValues: List<String>
    private val mColumnDeps: SparseArray<Set<Int>>
    private val mCheckBoxes: Array<Array<CheckBox>>

    private val numRows: Int
    private val numCols: Int

    private lateinit var mValues: Map<String, MutableSet<String>>
    private lateinit var mSelectedValues: Map<String, MutableSet<String>>

    init {
        val styledAttrs = context.theme
                .obtainStyledAttributes(attrs, R.styleable.MultiSelectMultiListPreference, 0, 0)
        mEntries = styledAttrs
                .getTextArray(R.styleable.MultiSelectMultiListPreference_android_entries)
                .map(CharSequence::toString)
        mEntryValues = styledAttrs
                .getTextArray(R.styleable.MultiSelectMultiListPreference_android_entryValues)
                .map(CharSequence::toString)
        mColumnEntries = styledAttrs
                .getTextArray(R.styleable.MultiSelectMultiListPreference_columnEntries)
                .map(CharSequence::toString)
        mColEntryValues = styledAttrs
                .getTextArray(R.styleable.MultiSelectMultiListPreference_columnEntryValues)
                .map(CharSequence::toString)
        val columnDepsId = styledAttrs
                .getResourceId(R.styleable.MultiSelectMultiListPreference_columnDependencies, -1)
        styledAttrs.recycle()

        numRows = mEntries.size
        numCols = mColumnEntries.size

        // gather the data on dependencies between columns
        val colDeps = context.resources.obtainTypedArray(columnDepsId)
        mColumnDeps = SparseArray<Set<Int>>(colDeps.length())
        (0 until colDeps.length())
                .map { context.resources.getStringArray(colDeps.getResourceId(it, -1)) }
                .map { it.map { mColEntryValues.indexOf(it) } }
                .forEach { mColumnDeps.put(it[0], it.drop(1).toSet()) }
        colDeps.recycle()

        // create the check box 2d array
        mCheckBoxes = Array<Array<CheckBox>>(numRows) {
            Array<CheckBox>(numCols) { CheckBox(context) }
        }
    }

    override fun onCreateDialogView(): View {
        // create a table with a grid of checkboxes
        val table = TableLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            )
            setColumnStretchable(0, true) // the 0th column should stretch to fill the dialog
            val padding = DIALOG_PADDING.dpToPx(context)
            setPaddingRelative(padding, padding, padding, padding)
        }

        // create header row
        val header = TableRow(context)

        // params for the first row
        table.addView(header, TableLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ))

        // create the column headings and put them in the header row
        val headingParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER_HORIZONTAL }

        val headingPad = HEADING_PADDING.dpToPx(context)
        for (j in 0 until numCols) header.addView(
                TextView(context).apply {
                    text = mColumnEntries[j]
                    setPadding(headingPad, headingPad, headingPad, headingPad)
                },
                headingParams.apply { column = j }
        )

        // create the rest of rows
        val rowParams = TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val entryParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
            column = 0
        }
        for (i in mCheckBoxes.indices) {
            val row = TableRow(context)

            // create the row label and add it to the row
            row.addView(TextView(context).apply { text = mEntries[i] }, entryParams)

            // create the checkboxes
            for (j in mCheckBoxes[i].indices)
            {
                val checkBox = mCheckBoxes[i][j]

                // Set the check change listener
                val dependents = mColumnDeps[j]
                checkBox.setOnCheckedChangeListener(
                        dependents?.run { IndependentCheckChangeListener(i, j, this) }
                                ?: DependentCheckChangeListener(i, j)
                )

                (checkBox.parent as ViewGroup?)?.removeView(checkBox)

                row.addView(
                        checkBox,
                        TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.CENTER_HORIZONTAL
                            column = j
                        }
                )
            }
            table.addView(row, rowParams)
        }

        return table
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        updateCheckStates(mSelectedValues)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) persistValues(mSelectedValues)
        else mSelectedValues = cloneValues(mValues)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        persistValues(
                if (restorePersistedValue) getValuesFromResources(null)
                else defaultValue as? Map<String, MutableSet<String>>
        )
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        val array = context.resources.obtainTypedArray(a.getResourceId(index, -1))
        val values = (0 until numCols).associate { j ->
            j.asCol() to array.getTextArray(j).map(CharSequence::toString).toMutableSet()
        }
        array.recycle()
        return values
    }

    // Note: do not remove the cloning of v. It is necessary for
    // new data to be written to the preferences. Android expects the output of
    // Preference.getStringSetPref to not be modified. Thus when it receives it back
    // in onDialogClosed, it just keeps the old data we got here
    // source: stackoverflow.com/questions/12528836/shared-preferences-only-saved-first-time
    // source: developer.android.com/reference/android/content/SharedPreferences.html
    //     "Objects that are returned from the various get methods must be treated as
    //      immutable by the application."
    private fun getValuesFromResources(defaultValue: Map<String, MutableSet<String>>?):
            Map<String, MutableSet<String>> = (0 until numCols).associate {
        val k = it.asCol()
        val v = sharedPreferences
                .getStringSet(key + k, defaultValue?.get(k))?.toMutableSet() ?: mutableSetOf()
        Pair(k, v)
    }

    private fun persistValues(values: Map<String, MutableSet<String>>?) = values?.let {
        mValues = values
        mSelectedValues = cloneValues(values)
        if (shouldPersist()) {
            editor.apply {
                putBoolean(key, true)
                values.forEach { (k, v) -> putStringSet(key + k, v.toSet()) }
            }.apply()
        }
    }

    private fun updateCheckStates(values: Map<String, Set<String>>) {
        for (i in mCheckBoxes.indices) for (j in mCheckBoxes[i].indices)
            mCheckBoxes[i][j].isChecked = values[j.asCol()]?.contains(mEntryValues[i]) ?: false
        for (k in 0 until mColumnDeps.size()) {
            val independentColNo = mColumnDeps.keyAt(k)
            val dependents = mColumnDeps.get(independentColNo)
            for (d in dependents) for (i in mCheckBoxes.indices)
                mCheckBoxes[i][d].isEnabled = mCheckBoxes[i][independentColNo].isChecked
        }
    }

    private fun Int.asCol() = mColEntryValues[this]

    private fun <K, T> cloneValues(values: Map<K, MutableSet<T>>): Map<K, MutableSet<T>>
            = values.entries.associate { Pair(it.key, it.value) }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        // Create instance of custom BaseSavedState
        // Set the state's value with the class member that holds current setting value
        return SavedState(superState).apply { values = mSelectedValues }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        // Check whether we saved the state in onSaveInstanceState
        state?.let {
            if (state.javaClass != SavedState::class.java) {
                // Didn't save the state, so call superclass
                super.onRestoreInstanceState(state)
                return
            }
        }

        // Cast state to custom BaseSavedState and pass to superclass
        val myState = state as SavedState
        super.onRestoreInstanceState(myState.getSuperState())

        // Set this Preference's widget to reflect the restored state
        mSelectedValues = myState.values
        updateCheckStates(mSelectedValues)
    }

    private class SavedState: Preference.BaseSavedState {
        // Member that holds the setting's value
        lateinit var values: Map<String, MutableSet<String>>

        constructor(superState: Parcelable): super(superState)

        constructor(source: Parcel): super(source) {
            val size = source.readInt()
            val keys = arrayOfNulls<String>(size)
            source.readStringArray(keys)
            values = keys.associate {
                val n = source.readInt()
                val value = arrayOfNulls<String>(n)
                source.readStringArray(value)
                it!! to value.map{it!!}.toMutableSet()
            }
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            val keysArray = values.keys.toTypedArray()
            dest.writeInt(keysArray.size)
            dest.writeStringArray(keysArray)
            for (v in values.values) {
                dest.writeInt(v.size)
                dest.writeStringArray(v.toTypedArray())
            }
        }

        companion object {
            // Standard creator object using an instance of this class
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object: Parcelable.Creator<SavedState> {
                override fun createFromParcel(inParcel: Parcel) = SavedState(inParcel)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    private open inner class DependentCheckChangeListener(val mI: Int, val mJ: Int):
            CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            val rowEntry = mEntryValues[mI]
            val colEntry = mJ.asCol()
            if (isChecked) mSelectedValues[colEntry]?.add(rowEntry)
            else mSelectedValues[colEntry]?.remove(rowEntry)
        }
    }

    private inner class IndependentCheckChangeListener(i: Int, j: Int, val mDependents: Set<Int>):
            DependentCheckChangeListener(i, j) {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            super.onCheckedChanged(buttonView, isChecked)
            for (d in mDependents) {
                mCheckBoxes[mI][d].isEnabled = isChecked
                mCheckBoxes[mI][d].isChecked = false
            }
        }
    }

    companion object {
        private const val DIALOG_PADDING = 14.0f
        private const val HEADING_PADDING = 3.0f
    }
}