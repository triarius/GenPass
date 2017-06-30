package com.example.narthana.genpass

import android.content.ContentValues
import android.content.Context
import com.example.narthana.genpass.data.NewWordDBHelper
import com.example.narthana.genpass.data.WordContract
import java.io.InputStream
import java.security.SecureRandom
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

fun shuffle(array: CharArray, r: SecureRandom) {
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
fun shuffleFirst(array: IntArray, n: Int, r: SecureRandom) {
    fun swapInt(i: Int, j: Int) {
        if (i != j) {
            val temp = array[i]
            array[i] = array[j]
            array[j] = temp
        }
    }

    for (i in 0..n - 1) swapInt(i, r.nextInt(array.lastIndex) + i)
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
