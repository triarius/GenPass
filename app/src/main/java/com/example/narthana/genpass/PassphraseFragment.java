package com.example.narthana.genpass;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.narthana.genpass.WordContract.WordEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by narthana on 22/10/16.
 */

public class PassphraseFragment extends Fragment
{
    private final String WORDS_TAG = "words";
    private String[] mWords;
    private boolean mAsyncComplete;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_passphrase, container, false);

        final int n = 4;
        final int maxWordLength = 10;
        final int minWordLength = 5;
        final String delim = " ";


        final Random r = new Random();
        final Button btnGenerate = (Button) rootView.findViewById(R.id.button_generate_passphrase);

        if (savedInstanceState != null)
        {
            ArrayList<String> state = savedInstanceState.getStringArrayList(WORDS_TAG);
            if (state != null)
            {
                mWords = state.toArray(new String[0]);
                mAsyncComplete = true;
            }
        }
        else
        {
            AsyncTask task = new FetchWordListTask()
                    .execute(new Integer[] {minWordLength, maxWordLength});
        }

        btnGenerate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mAsyncComplete)
                {
                    for (int i = 0; i < n; ++i)
                    {
                        int j = r.nextInt(mWords.length - i) + i; // random int in [i, words.length)
                        swap(mWords, i, j);
                    }
                    String passphrase = concatenateRange(mWords, delim, 0, n - 1);

                    TextView passText = (TextView) rootView.findViewById(R.id.textview_passphrase);
                    passText.setText(passphrase);
                }
                else Snackbar.make(rootView, R.string.dict_load_snack, Snackbar.LENGTH_LONG).show();
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (mAsyncComplete)
        {
            outState.putStringArrayList(WORDS_TAG, new ArrayList<String>(Arrays.asList(mWords)));
//            WordsParceable wordsParcel = new WordsParceable(mWords);
//            outState.putParcelable(WORDS_TAG, wordsParcel);
        }
    }

    private <T> void swap(T[] array, int i, int j)
    {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    @NonNull
    private String concatenateRange(String[] words, String delim, int start, int end)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; ++i)
        {
            builder.append(words[i]);
            builder.append(delim);
        }
        builder.append(words[end]);
        return builder.toString();
    }


    // Fetch the words in the background
    public class FetchWordListTask extends AsyncTask<Integer[], Void, String[]>
    {
        @Override
        protected String[] doInBackground(Integer[]... params)
        {
            String[] columns = { WordEntry.COLUMN_WORD };
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

            String[] words = new String[n];
            c.moveToFirst();
            for (int i = 0; i < n; ++i, c.moveToNext()) words[i] = c.getString(0);

            c.close();
            db.close();
            return words;
        }

        @Override
        protected void onPostExecute(String[] result)
        {
            mAsyncComplete = true;
            mWords = result;
        }
    }
}
