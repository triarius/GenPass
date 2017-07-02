package com.example.narthana.genpass

import android.content.Context
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.example.narthana.genpass.data.NewWordDBHelper
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by narthana on 23/10/16.
 */

@RunWith(AndroidJUnit4::class)
class TestDB {
    private var mContext: Context? = null

    @Rule
    var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        mContext = mActivityRule.activity
    }

    @Test
    fun loadDictionary() {
        val dictionary = mContext!!.resources.openRawResource(R.raw.english)
        loadDictionary(mContext!!, dictionary)
        val db = NewWordDBHelper(mContext!!).readableDatabase
        val c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)

        assertTrue("Could not create database", c.moveToFirst())
        c.close()
        db.close()
    }
}
