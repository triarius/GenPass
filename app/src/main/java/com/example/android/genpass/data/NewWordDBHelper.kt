package com.example.android.genpass.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by narthana on 23/10/16.
 */

class NewWordDBHelper(context: Context):
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL("""
            CREATE TABLE ${WordContract.WordEntry.TABLE_NAME}
            (${WordContract.WordEntry._ID} INTEGER PRIMARY KEY,
             ${WordContract.WordEntry.COLUMN_WORD} TEXT UNIQUE NOT NULL,
             ${WordContract.WordEntry.COLUMN_LEN} INTEGER NOT NULL);
        """)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE ${WordContract.WordEntry.TABLE_NAME}")
        onCreate(sqLiteDatabase)
    }

    companion object {
        private const val DATABASE_NAME = "words.db"
        private const val DATABASE_VERSION = 1
    }
}
