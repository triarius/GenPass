package com.example.narthana.genpass;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
    private static final int DEFAULT_NUM_CHECKS = 1;

    private final CharSequence[] mEntries;
    private final CharSequence[] mEntryValues;
    private final CharSequence[] mColumnEntries;
    private final CharSequence[] mColEntryValues;
    private final CharSequence[] mColumnDependencies;

    private final int numRows;
    private final int numCols;

    private CheckBox[][] mCheckBoxes;
    private Map<String, Set<String>> mValues;
    private Map<String, Set<String>> mSelectedValues;

//    private boolean mPreferenceChanged;

    public MultiSelectMultiListPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        setPersistent(true);
        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MultiSelectMultiListPreference,
                0,
                0
        );

        mEntries = a.getTextArray(R.styleable.MultiSelectMultiListPreference_android_entries);
        mEntryValues = a.getTextArray(R.styleable.MultiSelectMultiListPreference_android_entryValues);
        mColumnEntries = a.getTextArray(R.styleable.MultiSelectMultiListPreference_columnEntries);
        mColEntryValues = a.getTextArray(R.styleable.MultiSelectMultiListPreference_columnEntryValues);
        mColumnDependencies = a.getTextArray(R.styleable.MultiSelectMultiListPreference_columnDependencies);
        a.recycle();

        numRows = mEntries.length;
        numCols = mColumnEntries.length;
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
        int padding = Utility.dpToPx(getContext(), 14);
        table.setPaddingRelative(padding, padding, padding, padding);

        // create header row
        TableRow header = new TableRow(getContext());

        // params for the first row
        TableLayout.LayoutParams fistRowParams = new TableLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        table.addView(header, fistRowParams);

        int pad = Utility.dpToPx(getContext(), 3);
        // loop to create the column headings and put them in the header row
        for (int j = 0; j < numCols;)
        {
            TextView headingTextView = new TextView(getContext());
            headingTextView.setText(mColumnEntries[j]);
            headingTextView.setPadding(pad, pad, pad, pad);
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

        mCheckBoxes = new CheckBox[numRows][numCols];

        for (int i = 0; i < numRows; ++i)
        {
            TableRow row = new TableRow(getContext());

            // create the row label and add it to the row
            TextView entryTextView = new TextView(getContext());
            entryTextView.setText(mEntries[i]);
            row.addView(entryTextView, entryParams);

            // create the checkboxes
            for (int j = 0; j < numCols;)
            {
                final int iPos = i;
                final int jPos = j;

                CheckBox checkBox = new CheckBox(getContext());

//                mCheckBoxes.get(String.valueOf(j)).put(mEntryValues[i].toString(), checkBox);
                mCheckBoxes[i][j] = checkBox;

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        String rowEntry = mEntryValues[iPos].toString();
//                        String colEntry = mColEntryValues[jPos].toString();
                        String colEntry = String.valueOf(jPos);

//                        mPreferenceChanged =
//                                !(mValues.get(colEntry).contains(rowEntry) == isChecked);

//                        if (mPreferenceChanged)
//                        {
                            if (isChecked) mSelectedValues.get(colEntry).add(rowEntry);
                            else mSelectedValues.get(colEntry).remove(rowEntry);
//                        }
                    }
                });

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
        updateCheckStates();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
//        if (positiveResult && mPreferenceChanged) persistValues(mSelectedValues);
//        if (!positiveResult) mSelectedValues = cloneValues(mValues);
//        mPreferenceChanged = false;

        if (positiveResult) persistValues(mSelectedValues);
        else mSelectedValues = cloneValues(mValues);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
    {
        persistValues(restorePersistedValue ? getValuesFromResources(mValues)
                                            : (Map<String , Set<String>>) defaultValue);
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
                    getKey() + String.valueOf(j),
                    defaultValue != null ? defaultValue.get(String.valueOf(j)) : null
            );
            if (list != null) values.put(String.valueOf(j), new HashSet<String>(list));
        }
        return values;
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

            values.put(String.valueOf(j), result);
        }

        array.recycle();
        return values;
    }

    private void persistValues(Map<String, Set<String>> values)
    {
        mValues = values;
        mSelectedValues = cloneValues(values);
        if (shouldPersist())
        {
//            Log.d(getClass().getSimpleName(), "Saving values");
//            Log.d(getClass().getSimpleName(), valuesToString(values));
            SharedPreferences.Editor editor = getEditor();
            editor.putBoolean(getKey(), true);
            for (Map.Entry<String, Set<String>> entry : values.entrySet())
                editor.putStringSet(getKey() + entry.getKey(), entry.getValue());
            editor.apply();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
//        Log.d(getClass().getSimpleName(), "Saving State");
//        Log.d(getClass().getSimpleName(), "selected values = " + valuesToString(mSelectedValues));
//        Log.d(getClass().getSimpleName(), "values = " + valuesToString(mValues));
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
//        Log.d(getClass().getSimpleName(), "Restoring state");
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
//        Log.d(getClass().getSimpleName(), "selected values = " + valuesToString(mSelectedValues));
//        Log.d(getClass().getSimpleName(), "values = " + valuesToString(mValues));
        updateCheckStates();
    }

    private static class SavedState extends BaseSavedState
    {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        Map<String, Set<String>> values;

        public SavedState(Parcelable superState)
        { super(superState); }

        public SavedState(Parcel source)
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
            Set<String> keySet = values.keySet();
            String[] keysArray = keySet.toArray(new String[keySet.size()]);
            dest.writeInt(keysArray.length);
            dest.writeStringArray(keysArray);
            for (String key : keysArray)
            {
                Set<String> value = values.get(key);
                dest.writeInt(value.size());
                dest.writeStringArray(value.toArray(new String[value.size()]));
            }
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>()
        {
            public SavedState createFromParcel(Parcel in) { return new SavedState(in); }
            public SavedState[] newArray(int size) { return new SavedState[size];}
        };
    }

    private void updateCheckStates()
    {
        if (mCheckBoxes != null)
            for (int i = 0; i < numRows; ++i) for (int j = 0; j < numCols; ++j)
                mCheckBoxes[i][j].setChecked(
                        mSelectedValues.get(String.valueOf(j))
                                .contains(mEntryValues[i].toString())
                );
    }

    private <K, T> Map<K, Set<T>> cloneValues(Map<K, Set<T>> values)
    {
        Map<K, Set<T>> newValues = new HashMap<>(values.size());
        for (Map.Entry<K, Set<T>> entry : values.entrySet())
            newValues.put(entry.getKey(), new HashSet<T>(entry.getValue()));
        return newValues;
    }

//    private String valuesToString(Map<String, Set<String>> values)
//    {
//        String keys = Arrays.toString(values.keySet().toArray());
//        StringBuilder valuesArrays = new StringBuilder();
//        for (Map.Entry<String, Set<String>> e : values.entrySet())
//            valuesArrays.append(Arrays.toString(e.getValue().toArray()));
//        return keys + valuesArrays.toString();
//    }
}
