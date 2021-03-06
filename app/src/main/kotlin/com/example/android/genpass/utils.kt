package com.example.android.genpass

import android.app.Activity
import android.app.Fragment
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.preference.PreferenceManager
import android.support.v4.widget.DrawerLayout
import android.util.Log
import android.view.Gravity
import com.example.android.genpass.data.NewWordDBHelper
import com.example.android.genpass.data.WordContract
import java.util.*

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

internal fun Iterable<CharSequence>.randomString(r: Random) = map { it[r.nextInt(it.length)] }

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

internal inline fun <reified T> Fragment.getPrefFromId(keyId: Int, defValueId: Int): T
        = when (T::class.java) {
    Set::class.java -> getPref<Set<String>>(
            getString(keyId), defValueId, SharedPreferences::getStringSet, stringArrayToSet) as T
    Int::class.javaPrimitiveType, Int::class.javaObjectType -> getPref<Int>(
            getString(keyId), defValueId, SharedPreferences::getInt, Resources::getInteger) as T
    String::class.java -> getPref<String>(
            getString(keyId), defValueId, SharedPreferences::getString, Resources::getString) as T
    Boolean::class.javaPrimitiveType, Boolean::class.javaObjectType -> getPref<Boolean>(
            getString(keyId), defValueId, SharedPreferences::getBoolean, Resources::getBoolean) as T
    else -> throw UnsupportedOperationException()
}

internal inline fun <T> Fragment.getPref(
        key: String,
        defId: Int,
        getVal: SharedPreferences.(String, T) -> T,
        getDefVal: Resources.(Int) -> T
) = PreferenceManager.getDefaultSharedPreferences(activity).getVal(key, resources.getDefVal(defId))

internal val stringArrayToSet: Resources.(Int) -> Set<String> = { getStringArray(it).toSet() }

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

internal inline fun <reified T> T.log(string: String?) = Log.d(T::class.java.simpleName, string)

internal inline fun <reified T> Fragment.getSysService(name: String)
        = activity.getSystemService(name) as T

internal sealed class Pass { abstract val text: String }
internal abstract class CopyablePass : Pass()
internal abstract class UncopyablePass : Pass()
internal class ValidPass(override val text: String): CopyablePass()
internal class InvalidPass(override val text: String): UncopyablePass()