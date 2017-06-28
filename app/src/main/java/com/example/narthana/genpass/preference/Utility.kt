package com.example.narthana.genpass.preference

import android.content.Context
import android.util.TypedValue

/**
 * Created by narthana on 26/01/17.
 */

internal object Utility {
    fun dpToPx(context: Context, length: Int): Int {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                length.toFloat(),
                context.resources.displayMetrics
        ).toInt()
    }
}