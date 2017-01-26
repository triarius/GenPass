package com.example.narthana.genpass;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.narthana.genpass.data.WordContract.WordEntry;
import com.example.narthana.genpass.data.PreBuiltWordDBHelper;

import java.util.Arrays;

/**
 * Created by narthana on 22/10/16.
 */

public class PassphraseFragment extends Fragment
{
    private final String WORDS_TAG = "words";
    private final String PASSPHRASE_TAG = "passphrase";
    private final String COPYABLE_TAG = "copyable";
    private final String MAX_WORD_LEN_TAG = "maxwordlen";
    private final String MIN_WORD_LEN_TAG = "minwordlen";

    private SharedPreferences mPrefs;

//    private Bundle mSavedInstanceState;
    private int[] mWordIds;
    private boolean mWordIdsReady;
    private boolean mPassphraseCopyable = false;
    private String mPassphrase;
    private int mMaxWordLength;
    private int mMinWordLength;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        mSavedInstanceState = savedInstanceState;

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (savedInstanceState != null)
        {
//            Log.d(getClass().getSimpleName(), "Restoring state");
            mPassphraseCopyable = savedInstanceState.getBoolean(COPYABLE_TAG);
            mPassphrase = savedInstanceState.getString(PASSPHRASE_TAG);
            mMaxWordLength = savedInstanceState.getInt(MAX_WORD_LEN_TAG);
            mMinWordLength = savedInstanceState.getInt(MIN_WORD_LEN_TAG);
            mWordIds = Utility.expandFromRanges(savedInstanceState.getIntArray(WORDS_TAG));
            if (mWordIds != null) mWordIdsReady = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_passphrase, container, false);
        final Button btnGenerate = (Button) rootView.findViewById(R.id.button_generate_passphrase);
        final TextView passText = (TextView) rootView.findViewById(R.id.textview_passphrase);

        if (mPassphrase != null) passText.setText(mPassphrase);

        // set click listeners
        btnGenerate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mWordIdsReady)
                {
                    mPassphrase = createPhrase(mWordIds);
                    passText.setText(mPassphrase);
                    mPassphraseCopyable = true;
                }
                else Snackbar.make(
                        rootView,
                        R.string.dict_load_snack,
                        Snackbar.LENGTH_SHORT
                ).show();
            }
        });

        passText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mPassphraseCopyable)
                {
                    ClipboardManager clipboard = (ClipboardManager)
                            getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    final ClipData clip = ClipData.newPlainText(
                            getString(R.string.clipboard_text),
                            passText.getText()
                    );
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(rootView, R.string.copy_msg, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        final int maxWordLength = mPrefs.getInt(
                getString(R.string.pref_passphrase_max_word_length),
                getResources().getInteger(R.integer.passpharase_default_max_word_length)
        );
        final int minWordLength = mPrefs.getInt(
                getString(R.string.pref_passphrase_min_word_length),
                getResources().getInteger(R.integer.passpharase_default_min_word_length)
        );

        if (!mWordIdsReady || maxWordLength != mMaxWordLength || minWordLength != mMinWordLength)
        {
            mWordIdsReady = false;
            mMaxWordLength = maxWordLength;
            mMinWordLength = minWordLength;
            new FetchWordListTask().execute(new Integer[]{mMinWordLength, mMaxWordLength});
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (mPassphrase != null) outState.putString(PASSPHRASE_TAG, mPassphrase);
        if (mWordIdsReady) outState.putIntArray(WORDS_TAG, Utility.compressWithRanges(mWordIds));
        outState.putBoolean(COPYABLE_TAG, mPassphraseCopyable);
        outState.putInt(MAX_WORD_LEN_TAG, mMaxWordLength);
        outState.putInt(MIN_WORD_LEN_TAG, mMinWordLength);
    }

    private String createPhrase(int[] ids)
    {
        final int n = mPrefs.getInt(
                getString(R.string.pref_passphrase_num_words),
                getResources().getInteger(R.integer.pref_default_passphrase_num_words)
        );
        final String delim = mPrefs.getString(
                getString(R.string.pref_passphrase_delimiter),
                getString(R.string.passphrase_default_delimiter)
        );
        final boolean cap = mPrefs.getBoolean(
                getString(R.string.pref_passphrase_force_cap),
                getResources().getBoolean(R.bool.pref_default_passphrase_force_cap)
        );

        // choose random elements for the first n positions in the array
        Utility.shuffleN(mWordIds, n);

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

        if (cap) for (int i = 0; i < n; ++i)
            passphraseList[i] = passphraseList[i].substring(0, 1).toUpperCase()
                    + passphraseList[i].substring(1);

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
