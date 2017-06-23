package com.example.narthana.genpass

import android.content.ContentValues
import android.content.Context
import com.example.narthana.genpass.data.NewWordDBHelper
import com.example.narthana.genpass.data.WordContract
import java.io.InputStream
import java.util.*

/**
 * Created by narthana on 22/10/16.
 */

internal object Utility {
    private var r: Random? = null

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
//        try {
//            BufferedReader(InputStreamReader(dictionary)).use { br ->
//                NewWordDBHelper(context).writableDatabase.use { db ->
//                    db.beginTransaction()
//                    db.delete(WordEntry.TABLE_NAME, null, null)
//                    val content = ContentValues(2)
//                    while (br.readLine() != null) {
//                        content.clear()
//                        content.put(WordEntry.COLUMN_WORD, word)
//                        content.put(WordEntry.COLUMN_LEN, word.length)
//                        db.insert(WordEntry.TABLE_NAME, null, content)
//                    }
//                    db.setTransactionSuccessful()
//                    db.endTransaction()
//                }
//            }
//        } catch (e: IOException) {
//            Log.e(context::class.simpleName, "Could not read text file")
//            e.printStackTrace()
//        }
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

    fun expandFromRanges(input: IntArray): IntArray {
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
        return out
    }

    fun shuffle(array: CharArray) {
        if (r == null) r = Random()
        for (i in array.size - 1 downTo 1) swapChar(array, i, r!!.nextInt(i + 1))
    }

    // shuffle first n elements of the array
    fun shuffleN(array: IntArray, n: Int) {
        if (r == null) r = Random()
        for (i in 0..n - 1) {
            val j = r!!.nextInt(array.size - i) + i // random int in [i, words.length)
            swapInt(array, i, j)
        }
    }

    private fun swapChar(array: CharArray, i: Int, j: Int) {
        if (i != j) {
            val temp = array[i]
            array[i] = array[j]
            array[j] = temp
        }
    }

    private fun swapInt(array: IntArray, i: Int, j: Int) {
        val temp = array[i]
        array[i] = array[j]
        array[j] = temp
    }
}
