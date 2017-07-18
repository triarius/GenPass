package com.example.android.genpass.preference

import android.content.Context
import android.preference.EditTextPreference
import android.text.InputFilter
import android.util.AttributeSet
import com.example.android.genpass.R
import com.example.android.genpass.text.GraphemeLengthFilter

/**
 * Created by narthana on 26/01/17.
 */

class GraphemeEditTextPreference(context: Context, attrs: AttributeSet):
        EditTextPreference(context, attrs) {
    init {
        val maxLength = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.GraphemeEditTextPreference,
                0, 0
        ).use {
            getInt(R.styleable.GraphemeEditTextPreference_android_maxLength, Integer.MAX_VALUE)
        }

        super.getEditText().apply {
            filters = filters.map {
                if (it is InputFilter.LengthFilter) GraphemeLengthFilter(it.max) else it
            }.toTypedArray()
        }
    }
}