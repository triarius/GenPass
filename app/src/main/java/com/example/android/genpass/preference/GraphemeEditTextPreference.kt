package com.example.android.genpass.preference

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
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

        editText.filters = editText.filters.map {
            if (it is InputFilter.LengthFilter) {
                val max = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getMax(it)
                          else maxLength
                GraphemeLengthFilter(max)
            }
            else it
        }.toTypedArray()

        a.recycle()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getMax(filter: InputFilter.LengthFilter): Int {
        return filter.max
    }
}