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
    private val entries: List<String>
    private val entryValues: List<String>
    private val columnEntries: List<String>
    private lateinit var colEntryValues: List<String>
    private val columnDeps: SparseArray<Set<Int>>
    private val checkBoxes: Array<Array<CheckBox>>

    private val numRows: Int
    private val numCols: Int

    private lateinit var mValues: Map<String, Set<String>>
    private lateinit var mSelectedValues: Map<String, MutableSet<String>>

    init {
        val (entries, entryValues, columnEntries, colDepsId) = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.MultiSelectMultiListPreference,
                0, 0
        ).use {
            StyleAttrVals(
                    getTextArray(R.styleable.MultiSelectMultiListPreference_android_entries)
                            .map(CharSequence::toString),
                    getTextArray(R.styleable.MultiSelectMultiListPreference_android_entryValues)
                            .map(CharSequence::toString),
                    getTextArray(R.styleable.MultiSelectMultiListPreference_columnEntries)
                            .map(CharSequence::toString),
                    getResourceId(R.styleable.MultiSelectMultiListPreference_columnDependencies, -1)
            )
        }
        this.entries = entries
        this.entryValues = entryValues
        this.columnEntries = columnEntries

        numRows = this.entries.size
        numCols = this.columnEntries.size

        // gather the data on dependencies between columns
        columnDeps = context.resources.obtainTypedArray(colDepsId).use {
            SparseArray<Set<Int>>(length()).apply {
                (0 until length())
                        .map { context.resources.getStringArray(this@use.getResourceId(it, -1)) }
                        .map { it.map { colEntryValues.indexOf(it) } }
                        .forEach { put(it[0], it.drop(1).toSet()) }
            }
        }

        // create the check box 2d array
        checkBoxes = Array<Array<CheckBox>>(numRows) {
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
                    text = columnEntries[j]
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

        for (i in checkBoxes.indices) {
            val row = TableRow(context)

            // create the row label and add it to the row
            row.addView(TextView(context).apply { text = entries[i] }, entryParams)

            // create the checkboxes
            for (j in checkBoxes[i].indices) {
                val checkBox = checkBoxes[i][j]

                // Set the check change listener
                checkBox.setOnCheckedChangeListener(
                        columnDeps[j]?.run { IndependentCheckChangeListener(i, j, this) }
                                ?: DependentCheckChangeListener(i, j)
                )

                checkBox.removeFromParent()

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
        else mSelectedValues = makeSelectable(mValues)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        val def = defaultValue as? Map<String, Set<String>> ?: mapOf()
        persistValues(if (restorePersistedValue) getValuesFromResources(def) else def)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Map<String, Set<String>>
            = context.resources.obtainTypedArray(a.getResourceId(index, -1)).use {
        colEntryValues = getTextArray(0).map(CharSequence::toString)
        (1 until length()).associate {
            j -> (j - 1).asCol() to getTextArray(j).map(CharSequence::toString).toSet()
        }
    }

    // Note: do not remove the cloning of v. It is necessary for
    // new data to be written to the preferences. Android expects the output of
    // Preference.getStringSetPref to not be modified. Thus when it receives it back
    // in onDialogClosed, it just keeps the old data we got here
    // source: stackoverflow.com/questions/12528836/shared-preferences-only-saved-first-time
    // source: developer.android.com/reference/android/content/SharedPreferences.html
    //     "Objects that are returned from the various get methods must be treated as
    //      immutable by the application."
    private fun getValuesFromResources(defaultValue: Map<String, Set<String>>):
            Map<String, Set<String>> = (0 until numCols).associate {
        val k = it.asCol()
        k to (sharedPreferences.getStringSet(key + k, defaultValue.get(k)) ?: setOf())
    }

    private fun persistValues(values: Map<String, Set<String>>) {
        mValues = values
        mSelectedValues = makeSelectable(values)
        if (shouldPersist()) {
            editor.apply {
                putBoolean(key, true)
                values.forEach { (k, v) -> putStringSet(key + k, v) }
            }.apply()
        }
    }

    private fun updateCheckStates(values: Map<String, Set<String>>) {
        for (i in checkBoxes.indices) for (j in checkBoxes[i].indices)
            checkBoxes[i][j].isChecked = values[j.asCol()]?.contains(entryValues[i]) ?: false
        for (k in 0 until columnDeps.size()) {
            val independentColNo = columnDeps.keyAt(k)
            val dependents = columnDeps.get(independentColNo)
            for (d in dependents) for (i in checkBoxes.indices)
                checkBoxes[i][d].isEnabled = checkBoxes[i][independentColNo].isChecked
        }
    }

    private fun Int.asCol() = colEntryValues[this]

    private fun <K, T> makeSelectable(values: Map<K, Set<T>>): Map<K, MutableSet<T>>
            = values.entries.associate { Pair(it.key, it.value as MutableSet<T>) }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        // Create instance of custom BaseSavedState
        // Set the state's value with the class member that holds current setting value
        return SavedState(superState).apply { values = mSelectedValues }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || state.javaClass != SavedState::class.java) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state)
            return
        }

        // Cast state to custom BaseSavedState
        val myState = state as SavedState

        // Set this Preference's widget to reflect the restored state
        mSelectedValues = myState.values
        updateCheckStates(mSelectedValues)
        super.onRestoreInstanceState(myState.superState)
    }

    private class SavedState: Preference.BaseSavedState {
        lateinit var values: Map<String, MutableSet<String>>

        constructor(superState: Parcelable): super(superState)

        constructor(source: Parcel): super(source) {
            val keys = mutableListOf<String>()
            source.readStringList(keys)
            values = keys.associate {
                val value = mutableListOf<String>()
                source.readStringList(value)
                it to value.toMutableSet()
            }
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeStringList(values.keys.toList())
            for (v in values.values) dest.writeStringList(v.toList())
            super.writeToParcel(dest, flags)
        }

        companion object {
            @JvmField val CREATOR = creator(::SavedState)
        }
    }

    private open inner class DependentCheckChangeListener(val mI: Int, val mJ: Int):
            CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            val rowEntry = entryValues[mI]
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
                checkBoxes[mI][d].isEnabled = isChecked
                checkBoxes[mI][d].isChecked = false
            }
        }
    }

    private data class StyleAttrVals(val entries: List<String>, val entryValues: List<String>,
                                     val columnEntries: List<String>, val columnDepsId: Int)

    companion object {
        private const val DIALOG_PADDING = 14.0f
        private const val HEADING_PADDING = 3.0f
    }
}