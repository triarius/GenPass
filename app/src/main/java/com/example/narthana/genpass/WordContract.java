package com.example.narthana.genpass;

import android.provider.BaseColumns;

/**
 * Created by narthana on 23/10/16.
 */

final class WordContract
{
    // prevent accidental instantiation
    private WordContract() {}

    final static class WordEntry implements BaseColumns
    {
        static final String TABLE_NAME = "words";
        static final String COLUMN_WORD = "word";
        static final String COLUMN_LEN = "length";
    }
}
