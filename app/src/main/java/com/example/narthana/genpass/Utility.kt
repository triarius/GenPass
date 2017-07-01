package com.example.narthana.genpass

import android.app.Fragment
import android.content.ContentValues
import android.content.Context
import android.preference.PreferenceManager
import com.example.narthana.genpass.data.NewWordDBHelper
import com.example.narthana.genpass.data.WordContract
import java.io.InputStream
import java.util.*

/**
 * Created by narthana on 22/10/16.
 */

fun loadDictionary(context: Context, dictionary: InputStream) {
    NewWordDBHelper(context).writableDatabase.use { db ->
        db.beginTransaction()
        db.delete(WordContract.WordEntry.TABLE_NAME, null, null)
        val content = ContentValues(2)
        dictionary.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                content.run {
                    clear()
                    put(WordContract.WordEntry.COLUMN_WORD, line)
                    put(WordContract.WordEntry.COLUMN_WORD, line.length)
                    db.insert(WordContract.WordEntry.TABLE_NAME, null, this)
                }
            }
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }
}

fun compressWithRanges(input: IntArray): IntArray = input.foldIndexed(mutableListOf<Int>()) {
    i, list, x -> list.apply {
        if (i == 0) add(if (x + 1 == input[1]) x else -x)
        else if (i == input.lastIndex) add(if (x - 1 == input[i - 1]) x else -x)
        else if (x - 1 != input[i - 1] && x + 1 != input[i + 1]) add(-x)
        else if (x - 1 != input[i - 1] || x + 1 != input[i + 1]) add(x)
    }
}.toIntArray()

fun expandFromRanges(input: IntArray): IntArray {
    val (singles, ranges) = input.partition { it < 0 }
    val (starts, ends) = ranges.partitionIndexed { i, _ -> i % 2 == 0 }
    val expandedRanges = starts.zip(ends).flatMap { IntRange(it.first, it.second).toList() }
    return (singles.map { -it } + expandedRanges).toIntArray()
}

fun<T> Iterable<T>.partitionIndexed(predicate: (index: Int, T) -> Boolean): Pair<List<T>, List<T>> {
    val first = mutableListOf<T>()
    val second = mutableListOf<T>()
    var i = 0
    for (x in this) if (predicate(i++, x)) first.add(x) else second.add(x)
    return Pair(first, second)
}

fun shuffle(array: CharArray, r: Random) {
    fun swapChar(i: Int, j: Int) {
        if (i != j) {
            val temp = array[i]
            array[i] = array[j]
            array[j] = temp
        }
    }

    for (i in array.lastIndex downTo 1) swapChar(i, r.nextInt(i + 1))
}

// shuffle first n elements of the array
fun shuffleFirst(array: IntArray, n: Int, r: Random) {
    fun swapInt(i: Int, j: Int) {
        if (i != j) {
            val temp = array[i]
            array[i] = array[j]
            array[j] = temp
        }
    }

    for (i in 0 until n) swapInt(i, r.nextInt(array.lastIndex) + i)
}

/**
 * Selects [m] random elements from [array]. The selection uniform.
 *
 * @param array the array from which to obtain random elements
 * @m the number of randoms, must be in the range 1..[array.size]
 * @r an instance of [Random]
 * @return an array containing the random elements
 */
fun randomN(array: IntArray, m: Int, r: Random): IntArray {
    var ranges = linkedListOf(IntRange(0, array.lastIndex))
    val nums = IntArray(m) { 0 }
    for (i in 0 until m) {
        val temp = randFromRanges(ranges, r)
        ranges = temp.first
        nums[i] = array[temp.second]
    }
    return nums
}

val IntRange.len: Int
    get() = last - first + 1

/**
 * Takes a mutable list of ranges and picks a int uniformly at random from the ranges.
 * Splits the range at the chosen int and returns a Pair of the new list and the int
 *
 * @param ranges a list of [IntRange] from which to select a random integer
 * @param r an instance of [Random]
 * @return A [Pair]
 */
fun randFromRanges(ranges: LinkedList<IntRange>, r: Random): Pair<LinkedList<IntRange>, Int> {
    var hole = -1
    var rangeList = ranges.filter { !it.isEmpty() }
    val selection = rangeList.map { it.len }.sum()

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
            && Arrays.equals(other.array, array)
            && other.minWordLen == minWordLen
            && other.maxWordLen == maxWordLen
    override fun hashCode(): Int {
        var code = Arrays.hashCode(array)
        code += 31 * code + minWordLen
        code += 31 * code + maxWordLen
        return code
    }
}
object WordListError: WordListResult()

fun Fragment.getStringSet(key: String, defValues: Set<String>?): Set<String>? =
        PreferenceManager.getDefaultSharedPreferences(this.activity).getStringSet(key, defValues)
fun Fragment.getInt(key: String, defValues: Int): Int =
        PreferenceManager.getDefaultSharedPreferences(this.activity).getInt(key, defValues)
fun Fragment.getStringPref(key: String, defValues: String): String =
        PreferenceManager.getDefaultSharedPreferences(this.activity).getString(key, defValues)
fun Fragment.getBoolean(key: String, defValues: Boolean): Boolean =
        PreferenceManager.getDefaultSharedPreferences(this.activity).getBoolean(key, defValues)

