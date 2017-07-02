package com.example.android.genpass

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.example.android.genpass.data.NewWordDBHelper
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
    val mActivityRule = ActivityTestRule(MainActivity::class.java)
        @Rule get

    @Before
    fun setUp() {
    }

    @Test
    fun loadDictionary() {
        val dictionary = mActivityRule.activity.resources.openRawResource(R.raw.english)
        dictionary.bufferedReader().lineSequence().toDB(mActivityRule.activity)

        val db = NewWordDBHelper(mActivityRule.activity).readableDatabase
        val c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)

        assertTrue("Could not create database", c.moveToFirst())
        c.close()
        db.close()
    }
}