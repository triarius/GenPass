package com.example.narthana.genpass.text;

import android.text.InputFilter;
import android.text.Spanned;

import com.ibm.icu.text.BreakIterator;

import java.util.Locale;

/**
 * Created by narthana on 25/01/17.
 */

public class GraphemeLengthFilter implements InputFilter
{
    private final int mMax;
    private final Locale mLocale;

    public GraphemeLengthFilter(int max, Locale locale)
    {
        mMax = max;
        mLocale = locale;
    }

    public GraphemeLengthFilter(int max) { this(max, Locale.getDefault()); }

    public int getMax() { return mMax; }

    // We are replacing the range dstart...dend of dest with the rance start...end of source
    // The return value is the replacement or null to use source unadulterated
    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend)
    {
        //  number of graphemes we must keep
        int keep = mMax - (graphemeCount(dest, 0, dest.length())
                                 - graphemeCount(dest, dstart, dend));

        if (keep <= 0) return "";
        else if (keep >= graphemeCount(source, start, end)) return null; // keep original
        else
        {
            BreakIterator it = BreakIterator.getCharacterInstance(mLocale);
            it.setText(source.subSequence(start, end).toString());
            int keepOffSet = 0;
            // iterate though "kepp" graphemes, noting down the index in which we end up
            while (keep > 0 && (keepOffSet = it.next()) != BreakIterator.DONE) --keep;
            return source.subSequence(start, start + keepOffSet);
        }
    }

    private int graphemeCount(CharSequence seq, int beginIndex, int endIndex)
    {
        BreakIterator it = BreakIterator.getCharacterInstance(mLocale);
        it.setText(seq.subSequence(beginIndex, endIndex).toString());
        int count = 0;
        while (it.next() != BreakIterator.DONE) ++count;
        return count;
    }
}
