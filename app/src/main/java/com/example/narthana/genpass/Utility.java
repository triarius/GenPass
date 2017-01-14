package com.example.narthana.genpass;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.TypedValue;

import com.example.narthana.genpass.WordContract.WordEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by narthana on 22/10/16.
 */

class Utility
{
    private static Random r;

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

    static int[] compressWithRanges(int[] input)
    {
        List<Integer> ranges = new ArrayList<>(input.length / 4);
        ranges.add(input.length);
        for (int i = 0, n = input.length - 1; i <= n; ++i)
        {
            int j = i;
            while (i < n && input[i + 1] == input[i] + 1) ++i;
            if (i != j)
            {
                ranges.add(input[j]);
                ranges.add(input[i]);
            }
            else ranges.add(-input[i]);
        }
        int[] out = new int[ranges.size()];
        for (int i = 0; i < out.length; ++i) out[i] = ranges.get(i);
        return out;
    }

    static int[] expandFromRanges(int[] input)
    {
        int[] out = new int[input[0]];
        for (int i = 0, j = 1; j < input.length; )
        {
            if (input[j] < 0)
            {
                out[i++] = -input[j++];
            }
            else
            {
                int n = input[j + 1] - input[j] + 1;
                for (int k = 0; k < n; ++k, ++i) out[i] = input[j] + k;
                j += 2;
            }
        }
        return out;
    }

    static void shuffle(char[] array)
    {
        if (r == null) r = new Random();
        for (int i = array.length - 1; i > 0; --i) swapChar(array, i, r.nextInt(i + 1));
    }

    // shuffle first n elements of the array
    static void shuffleN(int[] array, int n)
    {
        if (r == null) r = new Random();
        for (int i = 0; i < n; ++i)
        {
            int j = r.nextInt(array.length - i) + i; // random int in [i, words.length)
            swapInt(array, i, j);
        }
    }

    private static void swapChar(char[] array, int i, int j)
    {
        if (i != j)
        {
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    private static void swapInt(int[] array, int i, int j)
    {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    static int dpToPx(Context context, int length)
    {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                length,
                context.getResources().getDisplayMetrics()
        );
    }
}
