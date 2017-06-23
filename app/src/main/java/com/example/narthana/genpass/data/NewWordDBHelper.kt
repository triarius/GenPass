package com.example.narthana.genpass.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by narthana on 23/10/16.
 */

class NewWordDBHelper(context: Context) : SQLiteOpenHelper(context, NewWordDBHelper.DATABASE_NAME, null, NewWordDBHelper.DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val CREATE_WORD_TABLE = """
            CREATE TABLE ${WordContract.WordEntry.TABLE_NAME}
            (${WordContract.WordEntry._ID} INTEGER PRIMARY KEY,
             ${WordContract.WordEntry.COLUMN_WORD} TEXT UNIQUE NOT NULL,
             ${WordContract.WordEntry.COLUMN_LEN} INTEGER NOT NULL);
        """
        sqLiteDatabase.execSQL(CREATE_WORD_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        val DROP_WORD_TABLE = "DROP TABLE " + WordContract.WordEntry.TABLE_NAME
        sqLiteDatabase.execSQL(DROP_WORD_TABLE)
        onCreate(sqLiteDatabase)
    }

    companion object {
        private val DATABASE_NAME = "words.db"
        private val DATABASE_VERSION = 1
    }
}
