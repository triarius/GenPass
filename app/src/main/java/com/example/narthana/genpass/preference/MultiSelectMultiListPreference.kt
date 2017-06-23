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
import java.util.*

/**
 * Created by narthana on 6/01/17.
 */

class MultiSelectMultiListPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
                                                               defStyleRes: Int = defStyleAttr): DialogPreference(context, attrs) {
    private val mEntries: Array<CharSequence>
    private val mEntryValues: Array<CharSequence>
    private val mColumnEntries: Array<CharSequence>
    private val mColEntryValues: Array<CharSequence>
    private val mColumnDependencies: SparseArray<Set<Int>>

    private val numRows: Int
    private val numCols: Int

    private var mCheckBoxes: Array<Array<CheckBox?>>? = null
    private var mValues: Map<String, MutableSet<String>>? = null
    private var mSelectedValues: MutableMap<String, MutableSet<String>>? = null

    init {

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.MultiSelectMultiListPreference,
                0,
                0
        )

        mEntries = a.getTextArray(R.styleable.MultiSelectMultiListPreference_android_entries)
        mEntryValues = a.getTextArray(R.styleable.MultiSelectMultiListPreference_android_entryValues)
        mColumnEntries = a.getTextArray(R.styleable.MultiSelectMultiListPreference_columnEntries)
        mColEntryValues = a.getTextArray(R.styleable.MultiSelectMultiListPreference_columnEntryValues)
        val columnDepsId = a.getResourceId(R.styleable.MultiSelectMultiListPreference_columnDependencies, -1)
        a.recycle()

        numRows = mEntries.size
        numCols = mColumnEntries.size

        // gather the data on dependencies between columns
        val b = context.resources.obtainTypedArray(columnDepsId)
        mColumnDependencies = SparseArray<Set<Int>>(b.length())

        val n = b.length()
        for (j in 0 .. n - 1) {
            val c = context.resources.getStringArray(b.getResourceId(j, -1))
            val depIndices = toIndices(c)

            val depSet = HashSet<Int>()
            for (i in 1..depIndices.size - 1) depSet.add(depIndices[i])

            mColumnDependencies.put(depIndices[0], depSet)
        }

        b.recycle()
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
            val padding = Utility.dpToPx(context, 14)
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

        val padding = Utility.dpToPx(context, 3)
        // loop to create the column headings and put them in the header row
        run {
            var j = 0
            while (j < numCols) {
                val headingTextView = TextView(context)
                headingTextView.text = mColumnEntries[j]
                headingTextView.setPadding(padding, padding, padding, padding)
                val headingParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                )
                headingParams.gravity = Gravity.CENTER_HORIZONTAL
                headingParams.column = ++j
                header.addView(headingTextView, headingParams)
            }
        }

        // create the rest of rows
        val rowParams = TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // we can recycles the params for the row labels, so take them out of the loop
        val entryParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        )
        entryParams.gravity = Gravity.CENTER_VERTICAL
        entryParams.column = 0

        // Creating the check boxes
        mCheckBoxes = Array<Array<CheckBox?>>(numRows) { arrayOfNulls<CheckBox>(numCols) }
        for (i in 0..numRows - 1) {
            val row = TableRow(context)

            // create the row label and add it to the row
            val entryTextView = TextView(context)
            entryTextView.text = mEntries[i]
            row.addView(entryTextView, entryParams)

            // create the checkboxes
            var j = 0
            while (j < numCols)
            // Note: increment near end of loop
            {
                val checkBox = CheckBox(context)
                mCheckBoxes!![i][j] = checkBox

                // Set the check change listener
                val dependents = mColumnDependencies.get(j)
                if (dependents != null)
                    checkBox.setOnCheckedChangeListener(
                            IndependentCheckChangeListener(i, j, dependents))
                else
                    checkBox.setOnCheckedChangeListener(DependentCheckChangeListener(i, j))

                val checkBoxParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                )
                checkBoxParams.gravity = Gravity.CENTER_HORIZONTAL
                checkBoxParams.column = ++j

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
        if (positiveResult) persistValues(mSelectedValues!!)
        else mSelectedValues = cloneValues(mValues!!)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        persistValues(
                if (restorePersistedValue) getValuesFromResources(mValues)
                else defaultValue as Map<String, MutableSet<String>>
        )
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        val res = context.resources
        val values = HashMap<String, Set<String>>()
        val array = res.obtainTypedArray(a.getResourceId(index, -1))

        val n = array.length()
        for (j in 0 .. n - 1) {
            val result = HashSet<String>()
            val value = array.getTextArray(j)
            for (c in value) result.add(c.toString())
            values.put(colValue(j), result)
        }

        array.recycle()
        return values
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        //        // Check whether this Preference is persistent (continually saved)
        //        // No need to save instance state since it's persistent,use superclass state
        //        if (isPersistent()) return superState;

        // Create instance of custom BaseSavedState
        val myState = SavedState(superState)
        // Set the state's value with the class member that holds current
        // setting value
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
        val myState = state as SavedState?
        super.onRestoreInstanceState(myState!!.getSuperState())

        // Set this Preference's widget to reflect the restored state
        mSelectedValues = myState.values
        updateCheckStates(mSelectedValues!!)
    }

    private fun getValuesFromResources(defaultValue: Map<String, MutableSet<String>>?): Map<String, MutableSet<String>> {
        val values = HashMap<String, MutableSet<String>>(numCols)

        val prefs = sharedPreferences
        for (j in 0..numCols - 1) {
            // Note: do not remove the cloning of the list variable. It is necessary for
            // new data to be written to the preferences. Android expects the output of
            // Preference.getStringSet to not be modified. Thus when it receives it back
            // in onDialogClosed, it just keeps the old data we got here
            // source: stackoverflow.com/questions/12528836/shared-preferences-only-saved-first-time
            // source: developer.android.com/reference/android/content/SharedPreferences.html
            //     "Objects that are returned from the various get methods must be treated as
            //      immutable by the application."

            val list = prefs.getStringSet(
                    key + colValue(j),
                    if (defaultValue != null) defaultValue[colValue(j)] else null
            )
            if (list != null) values.put(colValue(j), HashSet(list))
        }
        return values
    }

    private fun persistValues(values: Map<String, MutableSet<String>>) {
        mValues = values
        mSelectedValues = cloneValues(values)
        if (shouldPersist()) {
            val editor = editor
            editor.putBoolean(key, true)
            for ((key, value) in values)
                editor.putStringSet(key + key, value)
            editor.apply()
        }
    }

    private fun updateCheckStates(values: Map<String, Set<String>>) {
        if (mCheckBoxes != null) {
            for (i in 0..numRows - 1)
                for (j in 0..numCols - 1)
                    mCheckBoxes!![i][j]!!.isChecked = values[colValue(j)]!!.contains(mEntryValues[i].toString())
            for (k in 0..mColumnDependencies.size() - 1) {
                val independentColNo = mColumnDependencies.keyAt(k)
                val dependents = mColumnDependencies.get(independentColNo)
                for (d in dependents)
                    for (i in 0..numRows - 1)
                        mCheckBoxes!![i][d]!!.isEnabled = mCheckBoxes!![i][independentColNo]!!.isChecked
            }
        }
    }

    private fun colValue(j: Int): String {
        return j.toString()
        //        return mColEntryValues[j];
    }

    private fun <K, T> cloneValues(values: Map<K, MutableSet<T>>): MutableMap<K, MutableSet<T>> {
        val newValues = HashMap<K, MutableSet<T>>(values.size)
        for ((key, value) in values) newValues.put(key, HashSet(value))
        return newValues
    }

    private fun toIndices(entryValues: Array<String>): IntArray {
        val indices = IntArray(entryValues.size)
        for (i in entryValues.indices)
            indices[i] = getColIndex(entryValues[i])
        return indices
    }

    private fun getColIndex(colValue: String): Int {
        // do a linear search
        for (i in mColEntryValues.indices)
            if (mColEntryValues[i] == colValue) return i
        return -1
    }

    private class SavedState : Preference.BaseSavedState {

        // Member that holds the setting's value
        internal var values: MutableMap<String, MutableSet<String>> = HashMap<String, MutableSet<String>>()

        internal constructor(superState: Parcelable) : super(superState) {}

        internal constructor(source: Parcel) : super(source) {
            // Get the current preference's value
            val size = source.readInt()
            values = HashMap<String, MutableSet<String>>(size)
            val keys = arrayOfNulls<String>(size)
            source.readStringArray(keys)
            for (key in keys) {
                val n = source.readInt()
                val value = arrayOfNulls<String>(n)
                source.readStringArray(value)
                values.put(key!!, HashSet(Arrays.asList<String>(*value)))
            }
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            // Write the preference's value
            val keysArray = values.keys.toTypedArray()
            dest.writeInt(keysArray.size)
            dest.writeStringArray(keysArray)
            for (key in keysArray) {
                val value = values[key]
                dest.writeInt(value!!.size)
                dest.writeStringArray(value.toTypedArray())
            }
        }

        companion object {
            // Standard creator object using an instance of this class
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    private open inner class DependentCheckChangeListener internal constructor(internal val mI: Int, internal val mJ: Int) : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            val rowEntry = mEntryValues[mI].toString()
            val colEntry = colValue(mJ)

            if (isChecked) mSelectedValues!![colEntry]!!.add(rowEntry)
            else mSelectedValues!![colEntry]!!.remove(rowEntry)
        }
    }

    private inner class IndependentCheckChangeListener internal constructor(i: Int, j: Int, private val mDependents: Set<Int>) : DependentCheckChangeListener(i, j) {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            super.onCheckedChanged(buttonView, isChecked)
            for (d in mDependents) {
                val dependentCheckBox = mCheckBoxes!![super.mI][d]
                dependentCheckBox!!.isEnabled = isChecked
                dependentCheckBox.isChecked = false
            }
        }
    }
}
