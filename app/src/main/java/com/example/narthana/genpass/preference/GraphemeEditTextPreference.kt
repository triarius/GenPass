package com.example.narthana.genpass.preference

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.preference.EditTextPreference
import android.text.InputFilter
import android.util.AttributeSet
import com.example.narthana.genpass.R
import com.example.narthana.genpass.text.GraphemeLengthFilter

/**
 * Created by narthana on 26/01/17.
 */

class GraphemeEditTextPreference : EditTextPreference {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context) : super(context) {
        //        init(context, null);
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val editText = super.getEditText()

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.GraphemeEditTextPreference,
                0,
                0
        )

        val maxLength = a.getInt(
                R.styleable.GraphemeEditTextPreference_android_maxLength,
                Integer.MAX_VALUE
        )

        val filters = editText.filters

        for (i in filters.indices) {
            if (filters[i] is InputFilter.LengthFilter) {
                val max = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    getMax(filters[i] as InputFilter.LengthFilter)
                else
                    maxLength
                filters[i] = GraphemeLengthFilter(max)
            }
        }

        editText.filters = filters
        a.recycle()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getMax(filter: InputFilter.LengthFilter): Int {
        return filter.max
    }
}