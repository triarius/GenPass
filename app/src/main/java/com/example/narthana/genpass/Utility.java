package com.example.narthana.genpass;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.narthana.genpass.WordContract.WordEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by narthana on 22/10/16.
 */

class Utility
{
    static void loadDictionary(Context context, InputStream dictionary)
    {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(dictionary));
             SQLiteDatabase db = new NewWordDBHelper(context).getWritableDatabase())
        {
            db.beginTransaction();
            db.delete(WordEntry.TABLE_NAME, null, null);
            ContentValues content = new ContentValues(2);
            for (String word; (word = br.readLine()) != null; )
            {
                content.clear();
                content.put(WordEntry.COLUMN_WORD, word);
                content.put(WordEntry.COLUMN_LEN, word.length());
                db.insert(WordEntry.TABLE_NAME, null, content);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        catch (IOException e)
        {
            Log.e(context.getClass().getName(), "Could not read text file");
            e.printStackTrace();
        }
    }
}
