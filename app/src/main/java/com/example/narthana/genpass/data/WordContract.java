package com.example.narthana.genpass.data;

import android.provider.BaseColumns;

/**
 * Created by narthana on 23/10/16.
 */

public final class WordContract
{
    // prevent accidental instantiation
    private WordContract() {}

    public final static class WordEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "words";
        public static final String COLUMN_WORD = "word";
        public static final String COLUMN_LEN = "length";
    }
}
