package com.example.android.genpass.text

import android.text.InputFilter
import android.text.Spanned
import com.ibm.icu.text.BreakIterator
import java.util.*

/**
 * Created by narthana on 25/01/17.
 */

class GraphemeLengthFilter @JvmOverloads
constructor(val max: Int, private val locale: Locale = Locale.getDefault()): InputFilter {
    /**
     * We are replacing the range [dstart]...[dend] of [dest] with the range [start]...[end]
     * of [source]. The return value is the replacement or null to use source unadulterated
     */
    override fun filter(source: CharSequence, start: Int, end: Int,
                        dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        //  keep = number of graphemes we must keep
        val keep = max - (graphemeCount(dest, 0, dest.length) - graphemeCount(dest, dstart, dend))

        return if (keep <= 0) ""
        else if (keep >= graphemeCount(source, start, end)) null // keep original
        else {
            // 0 < keep < graphemeCount(source, start, end).
            // so move as far into source as possible
            BreakIterator.getCharacterInstance(locale).run {
                setText(source.subSequence(start, end).toString())
                // iterate though "keep" graphemes, noting the index at which we end up
                val keepOffset = (1 .. keep).fold(0) { _, _ -> next() }
                source.subSequence(start, start + keepOffset)
            }
        }
    }

    /**
     * Counts the number of graphemes in [seq] from [beginIndex] to [endIndex]
     */
    private fun graphemeCount(seq: CharSequence, beginIndex: Int, endIndex: Int): Int
            = BreakIterator.getCharacterInstance(locale).run {
        setText(seq.subSequence(beginIndex, endIndex).toString())
        var count = 0
        while (next() != BreakIterator.DONE) ++count
        count
    }
}