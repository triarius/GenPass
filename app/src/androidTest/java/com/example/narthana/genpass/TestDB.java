package com.example.narthana.genpass;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

import static org.junit.Assert.assertTrue;

/**
 * Created by narthana on 23/10/16.
 */

@RunWith(AndroidJUnit4.class)
public class TestDB
{
    private Context mContext;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp()
    {
        mContext = mActivityRule.getActivity();
//        mContext.deleteDatabase(WordDbHelper.DATABASE_NAME);
    }

    @Test
    public void loadDictionary()
    {
        InputStream dictionary = mContext.getResources().openRawResource(R.raw.default_dictionary);
        Utility.loadDictionary(mContext, dictionary);
        SQLiteDatabase db = new WordDbHelper(mContext).getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Could not create database", c.moveToFirst());
        c.close();
        db.close();
    }
}
