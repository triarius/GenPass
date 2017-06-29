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
                content.clear()
                content.put(WordContract.WordEntry.COLUMN_WORD, line)
                content.put(WordContract.WordEntry.COLUMN_WORD, line.length)
                db.insert(WordContract.WordEntry.TABLE_NAME, null, content)
            }
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }
}

fun compressWithRanges(input: IntArray): IntArray {
    val ranges = ArrayList<Int>(input.size / 4)
    ranges.add(input.size)
    run {
        var i = 0
        val n = input.size - 1
        while (i <= n) {
            val j = i
            while (i < n && input[i + 1] == input[i] + 1) ++i
            if (i != j) {
                ranges.add(input[j])
                ranges.add(input[i])
            } else ranges.add(-input[i])
            ++i
        }
    }
    val out = IntArray(ranges.size)
    for (i in out.indices) out[i] = ranges[i]
    return out
}

fun expandFromRanges(input: IntArray): WordListResult {
    val out = IntArray(input[0])
    var i = 0
    var j = 1
    while (j < input.size) {
        if (input[j] < 0) {
            out[i++] = -input[j++]
        } else {
            val n = input[j + 1] - input[j] + 1
            var k = 0
            while (k < n) {
                out[i] = input[j] + k
                ++k
                ++i
            }
            j += 2
        }
    }
    return WordList(out)
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
data class WordList(val array: IntArray): WordListResult() {
    override fun equals(other: Any?): Boolean =
            other === this && other is WordList && Arrays.equals(other.array, array)
    override fun hashCode(): Int = Arrays.hashCode(array)
}
object WordListError: WordListResult()
