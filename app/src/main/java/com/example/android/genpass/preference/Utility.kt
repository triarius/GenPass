package com.example.android.genpass.preference

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup

/**
 * Created by narthana on 26/01/17.
 */

internal fun Float.dpToPx(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    context.resources.displayMetrics
).toInt()

internal inline fun <reified T> creator(crossinline f: (Parcel) -> T)
        = object: Parcelable.Creator<T> {
    override fun createFromParcel(source: Parcel): T = f(source)
    override fun newArray(n: Int): Array<T?> = arrayOfNulls<T>(n)
}

internal fun <T: View> T.removeFromParent() = (this.parent as? ViewGroup)?.removeView(this)