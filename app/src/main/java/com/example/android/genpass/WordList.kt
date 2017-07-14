package com.example.android.genpass

/**
 * Created by narthana on 14/07/17.
 */

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
object WordListLoading: WordListResult()

interface WordListListener {
    fun onWordListLoading()
    fun onWordListReady(words: WordListResult)
}