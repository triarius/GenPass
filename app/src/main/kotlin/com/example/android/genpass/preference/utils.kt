package com.example.android.genpass.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup

/**
 * Created by narthana on 26/01/17.
 */

fun <T> id(t: T): T = t

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

inline fun <T> TypedArray.use(f: TypedArray.() -> T): T {
    val y = f()
    recycle()
    return y
}

fun <T> Array<T>.mutaMap(mutate: T.() -> T) {
    for (i in this.indices) this[i] = this[i].mutate()
}