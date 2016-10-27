package com.example.narthana.genpass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.narthana.genpass.WordContract.WordEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * Created by narthana on 22/10/16.
 */

class Utility
{
    static String newPassword(int len)
    {
        Random r = new Random();
        String charSet = "!@#$%^&*()_+1234567890-=qwertyuiop[]asdfghjkl;'zxcvbnm,./QWERTYUIOP{}ASDFGHJKL:|\\\"ZXCVBNM<>?";
        char[] pass = new char[len];
        for (int i = 0; i < len; ++i)
            pass[i] = charSet.charAt(r.nextInt(charSet.length()));
        return new String(pass);
    }

    static void loadDictionary(Context context, InputStream dictionary)
    {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(dictionary));
             SQLiteDatabase db = new NewWordDBHelper(context).getWritableDatabase())
        {
            db.beginTransaction();
            db.delete(WordEntry.TABLE_NAME, null, null);
            ContentValues content = new ContentValues(2);
            for (String word; (word = br.readLine()) != null;)
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

    static String[] getWords(Context context, int minWordLen, int maxWordLen)
    {
        String[] columns = { WordEntry.COLUMN_WORD };
        String selection = WordEntry.COLUMN_LEN + " >= ?"
                + " AND " + WordEntry.COLUMN_LEN + " <= ?";
        String[] selectionArgs = { Integer.toString(minWordLen), Integer.toString(maxWordLen) };
//        SQLiteDatabase db = new NewWordDBHelper(context).getReadableDatabase();
        SQLiteDatabase db = new PreBuiltWordDBHelper(context).getReadableDatabase();

        Cursor c = db.query(
                WordEntry.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        int n = c.getCount();

        String[] words = new String[n];
        c.moveToFirst();
        for (int i = 0; i < n; ++i, c.moveToNext()) words[i] = c.getString(0);

        c.close();
        db.close();
        return words;
    }
}
