package com.example.android.genpass.preference

import android.content.Context
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

// https://hackernoon.com/kotlin-delegates-in-android-development-part-1-50346cf4aed7
private inline fun <T> SharedPreferences.delegate(
        key: String?,
        defaultValue: T,
        crossinline getter: SharedPreferences.(String, T) -> T,
        crossinline setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
): ReadWriteProperty<Any, T> = object: ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T
            = getter(key ?: property.name, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T)
            = edit().setter(key ?: property.name, value).apply()
}

internal fun SharedPreferences.boolean(key: String? = null, defaultValue: Boolean) =
        delegate(key, defaultValue, SharedPreferences::getBoolean, SharedPreferences.Editor::putBoolean)

internal fun SharedPreferences.int(key: String? = null, defaultValue: Int) =
        delegate(key, defaultValue, SharedPreferences::getInt, SharedPreferences.Editor::putInt)

internal fun SharedPreferences.string(key: String? = null, defaultValue: String) =
        delegate(key, defaultValue, SharedPreferences::getString, SharedPreferences.Editor::putString)

internal fun SharedPreferences.stringSet(key: String? = null, defaultValue: Set<String>) =
        delegate(key, defaultValue, SharedPreferences::getStringSet, SharedPreferences.Editor::putStringSet)