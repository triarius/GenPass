package com.example.android.genpass

import android.app.Fragment
import android.content.ContentValues
import android.content.Context
import android.preference.PreferenceManager
import com.example.android.genpass.data.NewWordDBHelper
import com.example.android.genpass.data.WordContract
import java.util.*

/**
 * Created by narthana on 22/10/16.
 */

fun Sequence<String>.toDB(context: Context) {
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

fun IntArray.compressWithRanges(): IntArray = if (size < 3) this else
    this.foldIndexed(mutableListOf<Int>()) { i, acc, x ->
        if (i == 0) acc.add(if (x + 1 == this[1]) x else -x)
        else if (i == this.lastIndex) acc.add(if (x - 1 == this[i - 1]) x else -x)
        else if (x - 1 != this[i - 1] && x + 1 != this[i + 1]) acc.add(-x)
        else if (x - 1 != this[i - 1] || x + 1 != this[i + 1]) acc.add(x)
        acc
    }.toIntArray()

fun IntArray.expandFromRanges(): IntArray {
    val (singles, ranges) = this.partition { it < 0 }
    val (starts, ends) = ranges.partitionIndexed { i, _ -> i % 2 == 0 }
    val expandedRanges = starts.zip(ends).flatMap { IntRange(it.first, it.second).toList() }
    return (singles.map { -it } + expandedRanges).toIntArray()
}

fun<T> Iterable<T>.partitionIndexed(predicate: (index: Int, T) -> Boolean):
        Pair<List<T>, List<T>> {
    val first = mutableListOf<T>()
    val second = mutableListOf<T>()
    var i = 0
    for (x in this) if (predicate(i++, x)) first.add(x) else second.add(x)
    return Pair(first, second)
}

/**
 * Shuffles a array of Chars inplace
 *
 * @param r an instance of [java.util.Random]
 */
fun CharArray.shuffle(r: Random) {
    fun swapChar(i: Int, j: Int) {
        if (i != j) {
            val temp = this[i]
            this[i] = this[j]
            this[j] = temp
        }
    }

    for (i in lastIndex downTo 1) swapChar(i, r.nextInt(i + 1))
}

/**
 * Selects [n] random elements. The selection uniform.
 *
 * @param n the number of randoms, must be in the range 1..[array.size]
 * @param r an instance of [java.util.Random]
 * @return an array containing the random elements
 */
fun IntArray.randomN(n: Int, r: Random): IntArray {
    var ranges = linkedListOf(IntRange(0, this.lastIndex))
    val nums = IntArray(n) { 0 }
    for (i in 0 until n) {
        val temp = ranges.random(r)
        ranges = temp.first
        nums[i] = this[temp.second]
    }
    return nums
}

val IntRange.len: Int
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
fun LinkedList<IntRange>.random(r: Random): Pair<LinkedList<IntRange>, Int> {
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
                        IntRange(hole + 1, p.head.endInclusive)
                ) + p.tail
            }
            else p.apply { tail = rec(tail, n - p.head.len) }
    }

    rangeList = rec(rangeList, selection)
    return Pair(rangeList, hole)
}

sealed class WordListResult
data class WordList(val array: IntArray, val minWordLen: Int, val maxWordLen: Int):
        WordListResult() {
    override fun equals(other: Any?): Boolean = other === this
            && other is WordList
            && other.array contentEquals array
            && other.minWordLen == minWordLen
            && other.maxWordLen == maxWordLen
    override fun hashCode(): Int {
        var code = array.contentHashCode()
        code += 31 * code + minWordLen
        code += 31 * code + maxWordLen
        return code
    }
}
object WordListError: WordListResult()

fun Fragment.getStringSetPref(key: String, defValues: Set<String>?): Set<String>? =
        PreferenceManager.getDefaultSharedPreferences(this.activity).getStringSet(key, defValues)
fun Fragment.getIntPref(key: String, defValues: Int): Int =
        PreferenceManager.getDefaultSharedPreferences(this.activity).getInt(key, defValues)
fun Fragment.getStringPref(key: String, defValues: String): String =
        PreferenceManager.getDefaultSharedPreferences(this.activity).getString(key, defValues)
fun Fragment.getBooleanPref(key: String, defValues: Boolean): Boolean =
        PreferenceManager.getDefaultSharedPreferences(this.activity).getBoolean(key, defValues)

