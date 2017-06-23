package com.example.narthana.genpass.text

import android.text.InputFilter
import android.text.Spanned
import com.ibm.icu.text.BreakIterator
import java.util.*

/**
 * Created by narthana on 25/01/17.
 */

class GraphemeLengthFilter @JvmOverloads constructor(val max: Int, private val mLocale: Locale = Locale.getDefault()) : InputFilter {

    // We are replacing the range dstart...dend of dest with the rance start...end of source
    // The return value is the replacement or null to use source unadulterated
    override fun filter(source: CharSequence, start: Int, end: Int,
                        dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        //  number of graphemes we must keep
        var keep = max - (graphemeCount(dest, 0, dest.length) - graphemeCount(dest, dstart, dend))

        if (keep <= 0)
            return ""
        else if (keep >= graphemeCount(source, start, end))
            return null // keep original
        else
        // 0 < keep < graphemeCount(source, start, end). so move as far into source as possible
        {
            val it = BreakIterator.getCharacterInstance(mLocale)
            val text = source.subSequence(start, end).toString()
            it.setText(text)
            // iterate though "keep" graphemes, noting the index at which we end up
            var keepOffSet = 0
            while (keep > 0 && keepOffSet < end) {
                testForGrapheme(rainbowFlag, it, text)
                keepOffSet = it.next()
                --keep
            }
            val newEnd = start + keepOffSet
            return source.subSequence(start, newEnd)
        }
    }

    private fun graphemeCount(seq: CharSequence, beginIndex: Int, endIndex: Int): Int {
        val it = BreakIterator.getCharacterInstance(mLocale)
        val text = seq.subSequence(beginIndex, endIndex).toString()
        it.setText(text)
        var count = 0
        var pos = it.current()
        while (pos != BreakIterator.DONE && pos < text.length) {
            testForGrapheme(rainbowFlag, it, text)
            ++count
            pos = it.next()
        }
        return count
    }

    private fun testForGrapheme(grapheme: String, it: BreakIterator, text: String) {
        val pos = it.current()
        var graphemePres = pos + grapheme.length <= text.length
        if (graphemePres) {
            var i = 0
            while (graphemePres && i < grapheme.length) {
                graphemePres = graphemePres and (text[pos + i] == grapheme[i])
                ++i
            }
        }
        if (graphemePres) it.next()
    }

    companion object {
        private val rainbowFlag = "\uD83C\uDFF3\uFE0F\u200D\uD83C\uDF08"
    }
}
