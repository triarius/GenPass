package com.example.android.genpass.preference

import android.content.Context
import android.util.TypedValue

/**
 * Created by narthana on 26/01/17.
 */

internal fun Float.dpToPx(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    context.resources.displayMetrics
).toInt()