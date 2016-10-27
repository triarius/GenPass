package com.example.narthana.genpass;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by narthana on 26/10/16.
 */

public class PreBuiltWordDBHelper extends SQLiteAssetHelper
{
    private static final String DATABASE_NAME = "words.db";
    private static final int DATABASE_VERSION = 1;

    public PreBuiltWordDBHelper(Context context)
    { super(context, DATABASE_NAME, null, DATABASE_VERSION); }
}
