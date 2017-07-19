package com.example.android.genpass.preference

import android.content.Context
import android.preference.EditTextPreference
import android.text.InputFilter
import android.util.AttributeSet
import com.example.android.genpass.text.GraphemeLengthFilter

/**
 * Created by narthana on 26/01/17.
 */

class GraphemeEditTextPreference(context: Context, attrs: AttributeSet):
        EditTextPreference(context, attrs) {
    init {
        editText.apply {
            filters.mutaMap {
                if (this is InputFilter.LengthFilter) GraphemeLengthFilter(max) else this
            }
        }
    }
}