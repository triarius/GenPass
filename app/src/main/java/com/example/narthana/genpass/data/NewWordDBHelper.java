package com.example.narthana.genpass.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by narthana on 23/10/16.
 */

public class NewWordDBHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "words.db";
    private static final int DATABASE_VERSION = 1;

    public NewWordDBHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        final String CREATE_WORD_TABLE = "CREATE TABLE " + WordContract.WordEntry.TABLE_NAME + " ("
                + WordContract.WordEntry._ID + " INTEGER PRIMARY KEY" + ", "
                + WordContract.WordEntry.COLUMN_WORD + " TEXT UNIQUE NOT NULL" + ", "
                + WordContract.WordEntry.COLUMN_LEN + " INTEGER NOT NULL" + ");";
        sqLiteDatabase.execSQL(CREATE_WORD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        final String DROP_WORD_TABLE = "DROP TABLE " + WordContract.WordEntry.TABLE_NAME;
        sqLiteDatabase.execSQL(DROP_WORD_TABLE);
        onCreate(sqLiteDatabase);
    }
}
