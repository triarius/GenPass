package com.example.narthana.genpass.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.narthana.genpass.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by narthana on 6/01/17.
 */

public class MultiSelectMultiListPreference extends DialogPreference
{
    private final CharSequence[] mEntries;
    private final CharSequence[] mEntryValues;
    private final CharSequence[] mColumnEntries;
    private final CharSequence[] mColEntryValues;
    private final SparseArray<Set<Integer>> mColumnDependencies;

    private final int numRows;
    private final int numCols;

    private CheckBox[][] mCheckBoxes;
    private Map<String, Set<String>> mValues;
    private Map<String, Set<String>> mSelectedValues;


    public MultiSelectMultiListPreference(Context context) { this(context, null); }

    public MultiSelectMultiListPreference(Context context, AttributeSet attrs)
    { this(context, attrs, 0); }

    public MultiSelectMultiListPreference(Context context, AttributeSet attrs, int defStyleAttr)
    { this(context, attrs, defStyleAttr, defStyleAttr); }

    public MultiSelectMultiListPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                          int defStyleRes)
    {
//        super(context, attrs, defStyleAttr, defStyleRes);
        super(context, attrs);

        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MultiSelectMultiListPreference,
                0,
                0
        );

        mEntries =
                a.getTextArray(R.styleable.MultiSelectMultiListPreference_android_entries);
        mEntryValues =
                a.getTextArray(R.styleable.MultiSelectMultiListPreference_android_entryValues);
        mColumnEntries =
                a.getTextArray(R.styleable.MultiSelectMultiListPreference_columnEntries);
        mColEntryValues =
                a.getTextArray(R.styleable.MultiSelectMultiListPreference_columnEntryValues);
        final int columnDepsId =
                a.getResourceId(R.styleable.MultiSelectMultiListPreference_columnDependencies, -1);
        a.recycle();

        numRows = mEntries.length;
        numCols = mColumnEntries.length;

        // gather the data on dependencies between columns
        final TypedArray b = context.getResources().obtainTypedArray(columnDepsId);
        mColumnDependencies = new SparseArray<>(b.length());

        for (int j = 0, n = b.length(); j < n; ++j)
        {
            final String[] c = context.getResources().getStringArray(b.getResourceId(j, -1));
            final int[] depIndices = toIndices(c);

            Set<Integer> depSet = new HashSet<>();
            for (int i = 1; i < depIndices.length; ++i) depSet.add(depIndices[i]);

            mColumnDependencies.put(depIndices[0], depSet);
        }

        b.recycle();
    }

    @Override
    protected View onCreateDialogView()
    {
        // create a table with a grid of checkboxes
        TableLayout table = new TableLayout(getContext());
        table.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // the 0th column should stretch to fill the dialog
        table.setColumnStretchable(0, true);
        {
            final int padding = Utility.dpToPx(getContext(), 14);
            table.setPaddingRelative(padding, padding, padding, padding);
        }

        // create header row
        TableRow header = new TableRow(getContext());

        // params for the first row
        TableLayout.LayoutParams fistRowParams = new TableLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        table.addView(header, fistRowParams);

        final int padding = Utility.dpToPx(getContext(), 3);
        // loop to create the column headings and put them in the header row
        for (int j = 0; j < numCols;)
        {
            TextView headingTextView = new TextView(getContext());
            headingTextView.setText(mColumnEntries[j]);
            headingTextView.setPadding(padding, padding, padding, padding);
            TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            headingParams.gravity = Gravity.CENTER_HORIZONTAL;
            headingParams.column = ++j;
            header.addView(headingTextView, headingParams);
        }

        // create the rest of rows
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // we can recycles the params for the row labels, so take them out of the loop
        TableRow.LayoutParams entryParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        entryParams.gravity = Gravity.CENTER_VERTICAL;
        entryParams.column = 0;

        // Creating the check boxes
        mCheckBoxes = new CheckBox[numRows][numCols];
        for (int i = 0; i < numRows; ++i)
        {
            TableRow row = new TableRow(getContext());

            // create the row label and add it to the row
            TextView entryTextView = new TextView(getContext());
            entryTextView.setText(mEntries[i]);
            row.addView(entryTextView, entryParams);

            // create the checkboxes
            for (int j = 0; j < numCols;) // Note: increment near end of loop
            {
                CheckBox checkBox = new CheckBox(getContext());
                mCheckBoxes[i][j] = checkBox;

                // Set the check change listener
                final Set<Integer> dependents = mColumnDependencies.get(j);
                if (dependents != null) checkBox.setOnCheckedChangeListener(
                        new IndependentCheckChangeListener(i, j, dependents));
                else checkBox.setOnCheckedChangeListener(new DependentCheckChangeListener(i, j));

                TableRow.LayoutParams checkBoxParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                checkBoxParams.gravity = Gravity.CENTER_HORIZONTAL;
                checkBoxParams.column = ++j;

                row.addView(checkBox, checkBoxParams);
            }

            table.addView(row, rowParams);
        }

        return table;
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);
        updateCheckStates(mSelectedValues);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        if (positiveResult) persistValues(mSelectedValues);
        else mSelectedValues = cloneValues(mValues);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
    {
        persistValues(restorePersistedValue ? getValuesFromResources(mValues)
                                            : (Map<String , Set<String>>) defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        Resources res = getContext().getResources();
        Map<String, Set<String>> values = new HashMap<>();
        TypedArray array = res.obtainTypedArray(a.getResourceId(index, -1));

        for (int j = 0, n = array.length(); j < n; ++j)
        {
            final Set<String> result = new HashSet<>();
            CharSequence[] value = array.getTextArray(j);

            for (CharSequence c : value) result.add(c.toString());

            values.put(colValue(j), result);
        }

        array.recycle();
        return values;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
//        // Check whether this Preference is persistent (continually saved)
//        // No need to save instance state since it's persistent,use superclass state
//        if (isPersistent()) return superState;

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current
        // setting value
        myState.values = mSelectedValues;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class))
        {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        mSelectedValues = myState.values;
        updateCheckStates(mSelectedValues);
    }

    private Map<String, Set<String>> getValuesFromResources(Map<String, Set<String>> defaultValue)
    {
        Map<String, Set<String>> values = new HashMap<>(numCols);

        SharedPreferences prefs = getSharedPreferences();
        for (int j = 0; j < numCols; ++j)
        {
            // Note: do not remove the cloning of the list variable. It is necessary for
            // new data to be written to the preferences. Android expects the output of
            // Preference.getStringSet to not be modified. Thus when it receives it back
            // in onDialogClosed, it just keeps the old data we got here
            // source: stackoverflow.com/questions/12528836/shared-preferences-only-saved-first-time
            // source: developer.android.com/reference/android/content/SharedPreferences.html
            //     "Objects that are returned from the various get methods must be treated as
            //      immutable by the application."

            final Set<String> list = prefs.getStringSet(
                    getKey() + colValue(j),
                    defaultValue != null ? defaultValue.get(colValue(j)) : null
            );
            if (list != null) values.put(colValue(j), new HashSet<String>(list));
        }
        return values;
    }

    private void persistValues(Map<String, Set<String>> values)
    {
        mValues = values;
        mSelectedValues = cloneValues(values);
        if (shouldPersist())
        {
            SharedPreferences.Editor editor = getEditor();
            editor.putBoolean(getKey(), true);
            for (Map.Entry<String, Set<String>> entry : values.entrySet())
                editor.putStringSet(getKey() + entry.getKey(), entry.getValue());
            editor.apply();
        }
    }

    private void updateCheckStates(Map<String, Set<String>> values)
    {
        if (mCheckBoxes != null)
        {
            for (int i = 0; i < numRows; ++i)
                for (int j = 0; j < numCols; ++j)
                    mCheckBoxes[i][j].setChecked(
                            values
                                    .get(colValue(j))
                                    .contains(mEntryValues[i].toString())
                    );
            for (int k = 0; k < mColumnDependencies.size(); ++k)
            {
                final int independentColNo = mColumnDependencies.keyAt(k);
                Set<Integer> dependents = mColumnDependencies.get(independentColNo);
                for (Integer d : dependents)
                    for (int i = 0; i < numRows; ++i)
                        mCheckBoxes[i][d].setEnabled(mCheckBoxes[i][independentColNo].isChecked());
            }
        }
    }
    private String colValue(int j)
    {
        return String.valueOf(j);
//        return mColEntryValues[j];
    }

    private <K, T> Map<K, Set<T>> cloneValues(Map<K, Set<T>> values)
    {
        Map<K, Set<T>> newValues = new HashMap<>(values.size());
        for (Map.Entry<K, Set<T>> entry : values.entrySet())
            newValues.put(entry.getKey(), new HashSet<T>(entry.getValue()));
        return newValues;
    }

    private int[] toIndices(String[] entryValues)
    {
        final int[] indices = new int[entryValues.length];
        for (int i = 0; i < entryValues.length; ++i)
            indices[i] = getColIndex(entryValues[i]);
        return indices;
    }

    private int getColIndex(String colValue)
    {
        // do a linear search
        for (int i = 0; i < mColEntryValues.length; ++i)
            if (mColEntryValues[i].equals(colValue)) return i;
        return -1;
    }

    private static class SavedState extends BaseSavedState
    {
        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>()
                {
                    public SavedState createFromParcel(Parcel in) { return new SavedState(in); }
                    public SavedState[] newArray(int size) { return new SavedState[size];}
                };

        // Member that holds the setting's value
        Map<String, Set<String>> values;

        SavedState(Parcelable superState)
        { super(superState); }

        SavedState(Parcel source)
        {
            super(source);
            // Get the current preference's value
            int size = source.readInt();
            values = new HashMap<>(size);
            String[] keys = new String[size];
            source.readStringArray(keys);
            for (String key : keys)
            {
                int n = source.readInt();
                String[] value = new String[n];
                source.readStringArray(value);
                values.put(key, new HashSet<String>(Arrays.asList(value)));
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            String[] keysArray = values.keySet().toArray(new String[values.size()]);
            dest.writeInt(keysArray.length);
            dest.writeStringArray(keysArray);
            for (String key : keysArray)
            {
                Set<String> value = values.get(key);
                dest.writeInt(value.size());
                dest.writeStringArray(value.toArray(new String[value.size()]));
            }
        }
    }

    private class DependentCheckChangeListener implements CompoundButton.OnCheckedChangeListener
    {
        final int mI, mJ;

        DependentCheckChangeListener(int i, int j)
        {
            mI = i;
            mJ = j;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            String rowEntry = mEntryValues[mI].toString();
            String colEntry = colValue(mJ);

            if (isChecked) mSelectedValues.get(colEntry).add(rowEntry);
            else mSelectedValues.get(colEntry).remove(rowEntry);
        }
    }

    private class IndependentCheckChangeListener extends DependentCheckChangeListener
    {
        private final Set<Integer> mDependents;

        IndependentCheckChangeListener(int i, int j, Set<Integer> dependents)
        {
            super(i, j);
            mDependents = dependents;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            super.onCheckedChanged(buttonView, isChecked);
            for (Integer d : mDependents)
            {
                CheckBox dependentCheckBox = mCheckBoxes[super.mI][d];
                dependentCheckBox.setEnabled(isChecked);
                dependentCheckBox.setChecked(false);
            }
        }
    }
}
