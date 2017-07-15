package com.example.android.genpass.data

import android.provider.BaseColumns

/**
 * Created by narthana on 23/10/16.
 */

object WordContract {
    object WordEntry: BaseColumns {
        const val _ID = BaseColumns._ID
        const val TABLE_NAME = "words"
        const val COLUMN_WORD = "word"
        const val COLUMN_LEN = "length"
    }
}
