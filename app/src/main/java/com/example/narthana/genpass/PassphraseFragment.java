package com.example.narthana.genpass;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.narthana.genpass.WordContract.WordEntry;

import java.util.Arrays;

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
            mWordIds = Utility.expandFromRanges(savedInstanceState.getIntArray(WORDS_TAG));
            if (mWordIds != null) mWordIdsReady = true;
        }
        else new FetchWordListTask().execute(new Integer[] {minWordLength, maxWordLength});
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
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
                    Utility.shuffleN(mWordIds, n);
                    mPassphrase = createPhrase(mWordIds, delim, 0, n - 1);
                    passText.setText(mPassphrase);
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
        if (mWordIdsReady) outState.putIntArray(WORDS_TAG, Utility.compressWithRanges(mWordIds));
    }

    private String createPhrase(int[] ids, String delim, int start, int end)
    {
        int n = end - start + 1;
        if (n < 1) return "";

        // map the ids to Strings
        String[] selectionArgs = new String[n];
        for (int i = 0; i < n; ++i) selectionArgs[i] = Integer.toString(ids[i]);

        String selectionBase = WordEntry._ID + " = ?";
        String selectionDelim = " OR ";

        // create the WHERE clause of the SQL statement
        String[] selectionList = new String[n];
        for (int i = 0; i < n; ++i) selectionList[i] = selectionBase;
        String selection = TextUtils.join(selectionDelim, selectionList);

        SQLiteDatabase db = new PreBuiltWordDBHelper(getActivity()).getReadableDatabase();
        Cursor c = db.query(
                WordEntry.TABLE_NAME,
                new String[] { WordEntry.COLUMN_WORD },
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // we should retrieve every word
        if (c.getCount() != n)
        {
            Log.e(getClass().getSimpleName(), "Wrong size " + c.getCount() + " " + n);
            Log.d(getClass().getSimpleName(), selection);
            Log.e(getClass().getSimpleName(), Arrays.toString(selectionArgs));
            return "error";
        }

        // put the data in the cursor into an array so that it may be joined later
        String[] passphraseList = new String[n];
        c.moveToFirst();
        for (int i = 0; i < n; ++i, c.moveToNext()) passphraseList[i] = c.getString(0);

        c.close();
        db.close();
        return TextUtils.join(delim, passphraseList);
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

            SQLiteDatabase db = new PreBuiltWordDBHelper(getActivity()).getReadableDatabase();

            Cursor c = db.query(
                    WordEntry.TABLE_NAME,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (!c.moveToFirst())
            {
                Log.e(getClass().getSimpleName(), "Database Error");
                return null;
            }

            int n = c.getCount();
            int[] ids = new int[n];
            for (int i = 0; i < n; ++i, c.moveToNext()) ids[i] = c.getInt(0);

            c.close();
            db.close();
            return ids;
        }

        @Override
        protected void onPostExecute(int[] result)
        {
            mWordIdsReady = true;
            mWordIds = result;
        }
    }
}
