package com.example.android.genpass

import android.app.Activity
import android.app.Fragment
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import com.example.android.genpass.data.NewWordDBHelper
import com.example.android.genpass.data.WordContract
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by narthana on 22/10/16.
 */

internal fun Sequence<String>.toDB(context: Context) {
    NewWordDBHelper(context).writableDatabase.use { db ->
        db.beginTransaction()
        db.delete(WordContract.WordEntry.TABLE_NAME, null, null)
        val content = ContentValues(2)
        this.forEach { word ->
            db.insert(
                    WordContract.WordEntry.TABLE_NAME,
                    null,
                    content.apply {
                        clear()
                        put(WordContract.WordEntry.COLUMN_WORD, word)
                        put(WordContract.WordEntry.COLUMN_LEN, word.length)
                    }
            )
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }
}

internal fun IntArray.compressWithRanges(): IntArray = if (size < 3) this else
    this.foldIndexed(mutableListOf<Int>()) { i, acc, x ->
        if (i == 0) acc.add(if (x + 1 == this[1]) x else -x)
        else if (i == this.lastIndex) acc.add(if (x - 1 == this[i - 1]) x else -x)
        else if (x - 1 != this[i - 1] && x + 1 != this[i + 1]) acc.add(-x)
        else if (x - 1 != this[i - 1] || x + 1 != this[i + 1]) acc.add(x)
        acc
    }.toIntArray()

internal fun IntArray.expandFromRanges(): IntArray {
    val (singles, ranges) = this.partition { it < 0 }
    val (starts, ends) = ranges.partitionIndexed { i, _ -> i % 2 == 0 }
    val expandedRanges = starts.zip(ends).flatMap { IntRange(it.first, it.second).toList() }
    return (singles.map { -it } + expandedRanges).toIntArray()
}

internal fun<T> Iterable<T>.partitionIndexed(predicate: (index: Int, T) -> Boolean):
        Pair<List<T>, List<T>> {
    val first = mutableListOf<T>()
    val second = mutableListOf<T>()
    var i = 0
    for (x in this) if (predicate(i++, x)) first.add(x) else second.add(x)
    return Pair(first, second)
}

/**
 * Shuffles an array of Chars inplace
 *
 * @param r an instance of [java.util.Random]
 * @return the array
 */
internal fun CharArray.shuffle(r: Random): CharArray {
    fun swapChar(i: Int, j: Int) {
        if (i != j) {
            val temp = this[i]
            this[i] = this[j]
            this[j] = temp
        }
    }

    for (i in lastIndex downTo 1) swapChar(i, r.nextInt(i + 1))
    return this
}

/**
 * Selects [n] random elements. The selection is uniform.
 *
 * @param n the number of randoms, must be in the range 1..[array.size]
 * @param r an instance of [java.util.Random]
 * @return an array containing the random elements
 */
internal fun IntArray.randomN(n: Int, r: Random): IntArray {
    var ranges = linkedListOf(IntRange(0, this.lastIndex))
    val nums = IntArray(n) { 0 }
    for (i in 0 until n) {
        val temp = ranges.random(r)
        ranges = temp.first
        nums[i] = this[temp.second]
    }
    return nums
}

internal fun Iterable<Set<CharSequence>>.randomString(r: Random) = this.map {
    it.joinToString("").let { it[r.nextInt(it.length)] }
}

/** The length of the interval defined by the range */
internal val IntRange.len: Int
    get() = last - first + 1

/**
 * Picks an int uniformly at random from the ranges. Splits the range at the chosen int and
 * returns a Pair of the new list and the int
 *
 * @param r an instance of [Random]
 * @return A [Pair] consisting of the modified list of ranges and in integer
 *         If the random integer was not from the first range in the list, the return list will
 *         be identical to the receiver
 */
internal fun LinkedList<IntRange>.random(r: Random): Pair<LinkedList<IntRange>, Int> {
    var hole = -1
    var rangeList = this.filter { !it.isEmpty() }
    val selection = r.nextInt(rangeList.map { it.len }.sum())

    fun rec(p: LinkedList<IntRange>, n: Int): LinkedList<IntRange> = when (p) {
        is EmptyLinkedList -> p
        is NonEmptyLinkedList<IntRange> ->
            if (n < p.head.len) {
                hole = p.head.start + n
                linkedListOf(
                        IntRange(p.head.start, hole - 1),
                        IntRange(hole + 1, p.head.endInclusive),
                        tail = p.tail
                )
            }
            else p.apply { tail = rec(tail, n - p.head.len) }
    }

    rangeList = rec(rangeList, selection)
    return Pair(rangeList, hole)
}

internal fun Fragment.getStringSetPref(key: String, defValues: Set<String>?): Set<String> =
        PreferenceManager.getDefaultSharedPreferences(this.activity).getStringSet(key, defValues)
internal fun Fragment.getIntPref(key: String, defValues: Int): Int =
        PreferenceManager.getDefaultSharedPreferences(this.activity).getInt(key, defValues)
internal fun Fragment.getStringPref(key: String, defValues: String): String =
        PreferenceManager.getDefaultSharedPreferences(this.activity).getString(key, defValues)
internal fun Fragment.getBooleanPref(key: String, defValues: Boolean): Boolean =
        PreferenceManager.getDefaultSharedPreferences(this.activity).getBoolean(key, defValues)

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

/**
 * Perform the actions in [f] and return true
 */
internal inline fun consume(f: () -> Unit): Boolean {
    f()
    return true
}

/**
 * Perform the actions in [f] and close the drawer, returning true
 */
internal inline fun DrawerLayout.consume(f: () -> Unit): Boolean {
    f()
    closeDrawer(Gravity.START)
    return true
}

/**
 * Start an activity with a blank intent
 */
internal inline fun <reified T: Activity> Activity.startActivity()
        = startActivity(Intent(this, T::class.java))
/**
 * Replace the current fragment with the specified fragment
 */
internal fun Activity.replaceWith(
        fragment: Fragment,
        tag: String = fragment.javaClass.canonicalName
) = fragmentManager
        .beginTransaction()
        .replace(R.id.content_frame, fragment, tag)
        .addToBackStack(tag)
        .commit()

/**
 * If a fragment has been saved previously, retrieve it, else instantiate a new one
 */
internal inline fun <reified T: Fragment> Activity.findFragment(
        tag: String = T::class.java.canonicalName
) = fragmentManager.findFragmentByTag(tag) ?: T::class.java.newInstance()