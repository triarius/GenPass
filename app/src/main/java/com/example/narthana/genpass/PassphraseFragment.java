package com.example.narthana.genpass;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.narthana.genpass.WordContract.WordEntry;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by narthana on 22/10/16.
 */

public class PassphraseFragment extends Fragment
{
    private final String WORDS_TAG = "words";
    private final String PASSPHRASE_TAG = "passphrase";

    private int[] mWordIds;
    private boolean mWordIdsReady;
    private String mPassphrase;

    // TODO: put these in preferences
    private final int n = 4;
    private final int maxWordLength = 10;
    private final int minWordLength = 5;
    private final String delim = " ";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            mPassphrase = savedInstanceState.getString(PASSPHRASE_TAG);
            mWordIds = savedInstanceState.getIntArray(WORDS_TAG);
            if (mWordIds != null) mWordIdsReady = true;
        }
        else new FetchWordListTask().execute(new Integer[] {minWordLength, maxWordLength});
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final Random r = new Random();

        final View rootView = inflater.inflate(R.layout.fragment_passphrase, container, false);
        final Button btnGenerate = (Button) rootView.findViewById(R.id.button_generate_passphrase);
        final TextView passText = (TextView) rootView.findViewById(R.id.textview_passphrase);

        if (mPassphrase != null) passText.setText(mPassphrase);

        btnGenerate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mWordIdsReady)
                {
                    for (int i = 0; i < n; ++i)
                    {
                        int j = r.nextInt(mWordIds.length - i) + i; // random int in [i, words.length)
                        swap(mWordIds, i, j);
                    }
                    mPassphrase = createPhrase(mWordIds, delim, 0, n - 1);

                    passText.setText(mPassphrase);
                    Log.d(getClass().getSimpleName(), "OnClickListener " + mPassphrase);
                }
                else Snackbar.make(
                        rootView,
                        R.string.dict_load_snack,
                        Snackbar.LENGTH_SHORT
                ).show();
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (mPassphrase != null) outState.putString(PASSPHRASE_TAG, mPassphrase);
        if (mWordIdsReady) outState.putIntArray(WORDS_TAG, mWordIds);
//            outState.putStringArrayList(WORDS_TAG, new ArrayList<String>(Arrays.asList(mWords)));
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    private void swap(int[] array, int i, int j)
    {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    private String createPhrase(int[] ids, String delim, int start, int end)
    {
        int n = end - start + 1;

        // map the ids to Strings
        String[] selectionArgs = new String[n];
        for (int i = 0; i < n; ++i) selectionArgs[i] = Integer.toString(ids[i]);

        String selectionBase = WordEntry._ID + " = ?";
        String selectionDelim = " OR ";

        StringBuilder selection = new StringBuilder(selectionBase);
        for (int i = start + 1; i <= end; ++i)
        {
            selection.append(selectionDelim);
            selection.append(selectionBase);
        }


        SQLiteDatabase db = new PreBuiltWordDBHelper(getActivity()).getReadableDatabase();
        Cursor c = db.query(
                WordEntry.TABLE_NAME,
                new String[] { WordEntry.COLUMN_WORD },
                selection.toString(),
                selectionArgs,
                null,
                null,
                null
        );

        if (c.getCount() != n)
        {
            Log.e(this.getClass().getSimpleName(), "Wrong size " + c.getCount());
            Log.d(this.getClass().getSimpleName(), selection.toString());
            Log.e(this.getClass().getSimpleName(), Arrays.toString(selectionArgs));
            return "error";
        }

        StringBuilder builder = new StringBuilder();

        c.moveToFirst();
        builder.append(c.getString(0));
        for (int i = start + 1; i <= end; ++i)
        {
            c.moveToNext();
            builder.append(delim);
            builder.append(c.getString(0));
        }
        c.close();
        db.close();
        return builder.toString();
    }


    // Fetch the words in the background
    public class FetchWordListTask extends AsyncTask<Integer[], Void, int[]>
    {
        @Override
        protected int[] doInBackground(Integer[]... params)
        {
            String[] columns = { WordEntry._ID };
            String selection = WordEntry.COLUMN_LEN + " >= ?"
                    + " AND " + WordEntry.COLUMN_LEN + " <= ?";
            String[] selectionArgs = { Integer.toString(params[0][0]),
                                       Integer.toString(params[0][1]) };
//            Why can't we do this!
//            String[] dimens = Arrays.stream(params[0])
//                    .map(x -> Integer.toString(x))
//                    .toArray(String[]::new);

            SQLiteDatabase db = new PreBuiltWordDBHelper(getContext()).getReadableDatabase();

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

            int[] words = new int[n];
            c.moveToFirst();
            for (int i = 0; i < n; ++i, c.moveToNext()) words[i] = c.getInt(0);

            c.close();
            db.close();
            return words;
        }

        @Override
        protected void onPostExecute(int[] result)
        {
            mWordIdsReady = true;
            mWordIds = result;
        }
    }
}
