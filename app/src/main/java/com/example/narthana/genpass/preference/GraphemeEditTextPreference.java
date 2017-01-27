package com.example.narthana.genpass.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.EditTextPreference;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.EditText;

import com.example.narthana.genpass.R;
import com.example.narthana.genpass.text.GraphemeLengthFilter;

/**
 * Created by narthana on 26/01/17.
 */

public class GraphemeEditTextPreference extends EditTextPreference
{
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GraphemeEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                      int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public GraphemeEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public GraphemeEditTextPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    public GraphemeEditTextPreference(Context context)
    {
        super(context);
//        init(context, null);
    }

    private void init(Context context, AttributeSet attrs)
    {
        EditText editText = super.getEditText();

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.GraphemeEditTextPreference,
                0,
                0
        );

        final int maxLength = a.getInt(
                R.styleable.GraphemeEditTextPreference_android_maxLength,
                Integer.MAX_VALUE
        );

        InputFilter[] filters = editText.getFilters();

        for (int i = 0; i < filters.length; ++i)
        {
            if (filters[i] instanceof InputFilter.LengthFilter)
            {
                int max = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        ? getMax((InputFilter.LengthFilter) filters[i])
                        : maxLength;
                filters[i] = new GraphemeLengthFilter(max);
            }
        }

        editText.setFilters(filters);
        a.recycle();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private int getMax(InputFilter.LengthFilter filter) { return filter.getMax(); }
}