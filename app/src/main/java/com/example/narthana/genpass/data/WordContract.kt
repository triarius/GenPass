package com.example.narthana.genpass.data

import android.provider.BaseColumns

/**
 * Created by narthana on 23/10/16.
 */

object WordContract {
    class WordEntry: BaseColumns {
        companion object {
            val _ID = BaseColumns._ID
            val TABLE_NAME = "words"
            val COLUMN_WORD = "word"
            val COLUMN_LEN = "length"
        }
    }
}