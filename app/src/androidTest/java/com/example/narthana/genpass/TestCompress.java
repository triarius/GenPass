package com.example.narthana.genpass;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.narthana.genpass.data.PreBuiltWordDBHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * Created by narthana on 31/10/16.
 */

@RunWith(AndroidJUnit4.class)
public class TestCompress
{
    private Context mContext;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() { mContext = mActivityRule.getActivity(); }

    @Test
    public void compressOnRealData()
    {
        SQLiteDatabase db = new PreBuiltWordDBHelper(mContext).getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT _id FROM words WHERE length >= ? AND length <= ?",
                new String[] { Integer.toString(5), Integer.toString(10) }
        );
        c.moveToFirst();
        int n = c.getCount();
        int[] ids = new int[n];
        for (int i = 0; i < n; ++i, c.moveToNext()) ids[i] = c.getInt(0);
        c.close();

        int[] compressed = Utility.compressWithRanges(ids);
        assertTrue("Compression failed", Arrays.equals(ids, Utility.expandFromRanges(compressed)));
    }
}
