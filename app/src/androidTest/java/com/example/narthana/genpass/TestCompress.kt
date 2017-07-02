package com.example.narthana.genpass

import android.content.Context
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.example.narthana.genpass.data.PreBuiltWordDBHelper
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

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
                arrayOf(Integer.toString(5), Integer.toString(10))
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

        val compressed = compressWithRanges(ids)
        assertTrue("Compression failed", Arrays.equals(ids, expandFromRanges(compressed)))
    }
}
