package com.example.narthana.genpass;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by narthana on 6/01/17.
 */

public class MultiSelectMultiListPreference extends DialogPreference
{
    private static final int DEFAULT_NUM_CHECKS = 1;

//    private final AttributeSet mAttrs;
    private final CharSequence[] mEntries;
    private final CharSequence[] mEntryValues;
    private final CharSequence[] mColumnEntries;

    private Set<Set> mValues = new HashSet<>();
    private Set<Set> mNewValues = new HashSet<>();

    private boolean mPreferenceChanged;

    public MultiSelectMultiListPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

//        mAttrs = attrs;

        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MultiSelectMultiListPreference,
                0,
                0
        );

        mEntries = a.getTextArray(R.styleable.MultiSelectMultiListPreference_android_entries);
        mEntryValues = a.getTextArray(R.styleable.MultiSelectMultiListPreference_android_entryValues);
        mColumnEntries = a.getTextArray(R.styleable.MultiSelectMultiListPreference_columnEntries);



        a.recycle();
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
        // the 0th column should strech to fill to screen
        table.setColumnStretchable(0, true);
        int padding = dpToPx(14);
        table.setPaddingRelative(padding, padding, padding, padding);

        // create header row
        TableRow header = new TableRow(getContext());

        // params for the first row
        TableLayout.LayoutParams fistRowParams = new TableLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        table.addView(header, fistRowParams);

        // loop to create the column headings
        int colNum = 1; // top right cell should be blank
        for (CharSequence headingText : mColumnEntries)
        {
            TextView headingTextView = new TextView(getContext());
            headingTextView.setText(headingText);
            int p = dpToPx(2);
            headingTextView.setPadding(p, p, p, p);
            TableRow.LayoutParams headingParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            headingParams.gravity = Gravity.CENTER_HORIZONTAL;
            headingParams.column = colNum++;
            header.addView(headingTextView, headingParams);
        }

        // creat the rest of rows
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

        for (CharSequence entryText : mEntries)
        {
            TableRow row = new TableRow(getContext());

            // create the row label and add it to the row
            TextView entryTextView = new TextView(getContext());
            entryTextView.setText(entryText);
            row.addView(entryTextView, entryParams);

            // create the checkboxes
            colNum = 1;
            for (CharSequence c : mColumnEntries)
            {
                CheckBox checkBox = new CheckBox(getContext());
                TableRow.LayoutParams checkBoxParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                checkBoxParams.gravity = Gravity.CENTER_HORIZONTAL;
                checkBoxParams.column = colNum++;

                row.addView(checkBox, checkBoxParams);
            }

            table.addView(row, rowParams);
        }

        return table;
    }

    private int dpToPx(int length)
    {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                length,
                getContext().getResources().getDisplayMetrics()
        );
    }
}
