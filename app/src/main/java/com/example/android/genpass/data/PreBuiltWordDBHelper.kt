package com.example.android.genpass.data

import android.content.Context
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

/**
 * Created by narthana on 26/10/16.
 */

class PreBuiltWordDBHelper(context: Context):
        SQLiteAssetHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private val DATABASE_NAME = "words.db"
        private val DATABASE_VERSION = 1
    }
}