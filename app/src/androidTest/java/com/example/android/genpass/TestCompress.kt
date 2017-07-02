package com.example.android.genpass

import android.content.Context
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.example.android.genpass.data.PreBuiltWordDBHelper
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by narthana on 31/10/16.
 */

@RunWith(AndroidJUnit4::class)
class TestCompress {
    private var mContext: Context? = null

    @Rule
    var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        mContext = mActivityRule.activity
    }

    @Test
    fun compressOnRealData() {
        val db = PreBuiltWordDBHelper(mContext!!).readableDatabase
        val c = db.rawQuery(
                "SELECT _id FROM words WHERE length >= ? AND length <= ?",
                intArrayOf(5, 10).map(Int::toString).toTypedArray()
        )
        c.moveToFirst()
        val n = c.count
        val ids = IntArray(n)
        var i = 0
        while (i < n) {
            ids[i] = c.getInt(0)
            ++i
            c.moveToNext()
        }
        c.close()

        assertTrue(
                "Compression failed",
                ids contentEquals ids.compressWithRanges().expandFromRanges()
        )
    }
}
