package com.example.narthana.genpass;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.narthana.genpass.WordContract.WordEntry;

/**
 * Created by narthana on 23/10/16.
 */

class NewWordDBHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "words.db";
    private static final int DATABASE_VERSION = 1;

    NewWordDBHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        final String CREATE_WORD_TABLE = "CREATE TABLE " + WordEntry.TABLE_NAME + " ("
                + WordEntry._ID + " INTEGER PRIMARY KEY" + ", "
                + WordEntry.COLUMN_WORD + " TEXT UNIQUE NOT NULL" + ", "
                + WordEntry.COLUMN_LEN + " INTEGER NOT NULL" + ");";
        sqLiteDatabase.execSQL(CREATE_WORD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        final String DROP_WORD_TABLE = "DROP TABLE " + WordEntry.TABLE_NAME;
        sqLiteDatabase.execSQL(DROP_WORD_TABLE);
        onCreate(sqLiteDatabase);
    }
}
