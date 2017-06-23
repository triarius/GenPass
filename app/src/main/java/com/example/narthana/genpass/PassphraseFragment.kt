package com.example.narthana.genpass

import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.narthana.genpass.data.PreBuiltWordDBHelper
import com.example.narthana.genpass.data.WordContract.WordEntry

/**
 * Created by narthana on 22/10/16.
 */

class PassphraseFragment: Fragment() {
    private companion object {
        val WORDS_TAG = "words"
        val PASSPHRASE_TAG = "passphrase"
        val COPYABLE_TAG = "copyable"
        val MAX_WORD_LEN_TAG = "maxwordlen"
        val MIN_WORD_LEN_TAG = "minwordlen"
    }

    private var mWordIds: IntArray? = null
    private var mWordIdsReady = false
    private var mPassphraseCopyable = false
    private var mPassphrase: String? = null
    private var mMaxWordLength = 0
    private var mMinWordLength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            mPassphraseCopyable = savedInstanceState.getBoolean(COPYABLE_TAG)
            mPassphrase = savedInstanceState.getString(PASSPHRASE_TAG)
            mMaxWordLength = savedInstanceState.getInt(MAX_WORD_LEN_TAG)
            mMinWordLength = savedInstanceState.getInt(MIN_WORD_LEN_TAG)
            mWordIds = Utility.expandFromRanges(savedInstanceState.getIntArray(WORDS_TAG))
            if (mWordIds != null) mWordIdsReady = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_passphrase, container, false)
        val btnGenerate = rootView.findViewById(R.id.button_generate_passphrase) as Button
        val passText = rootView.findViewById(R.id.textview_passphrase) as TextView

        if (mPassphrase != null) passText.text = mPassphrase

        // set click listeners
        btnGenerate.setOnClickListener {
            if (mWordIdsReady) {
                mPassphrase = createPhrase(mWordIds!!)
                passText.text = mPassphrase
                mPassphraseCopyable = true
            } else Snackbar.make(
                        rootView,
                        R.string.dict_load_snack,
                        Snackbar.LENGTH_SHORT
                   ).show()
        }

        passText.setOnClickListener {
            if (mPassphraseCopyable) {
                val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                        getString(R.string.clipboard_text),
                        passText.text
                )
                clipboard.primaryClip = clip
                Snackbar.make(rootView, R.string.copy_msg, Snackbar.LENGTH_SHORT).show()
            }
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()

        val maxWordLength = prefs().getInt(
                getString(R.string.pref_passphrase_max_word_length),
                resources.getInteger(R.integer.passpharase_default_max_word_length)
        )
        val minWordLength = prefs().getInt(
                getString(R.string.pref_passphrase_min_word_length),
                resources.getInteger(R.integer.passpharase_default_min_word_length)
        )

        if (!mWordIdsReady || maxWordLength != mMaxWordLength || minWordLength != mMinWordLength) {
            mWordIdsReady = false
            mMaxWordLength = maxWordLength
            mMinWordLength = minWordLength
            FetchWordListTask().execute(Pair(mMinWordLength, mMaxWordLength))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mPassphrase != null) outState.putString(PASSPHRASE_TAG, mPassphrase)
        if (mWordIdsReady) outState.putIntArray(WORDS_TAG, Utility.compressWithRanges(mWordIds!!))
        outState.putBoolean(COPYABLE_TAG, mPassphraseCopyable)
        outState.putInt(MAX_WORD_LEN_TAG, mMaxWordLength)
        outState.putInt(MIN_WORD_LEN_TAG, mMinWordLength)
    }

    private fun createPhrase(ids: IntArray): String? {
        val n = prefs().getInt(
                getString(R.string.pref_passphrase_num_words),
                resources.getInteger(R.integer.pref_default_passphrase_num_words)
        )
        val delim = prefs().getString(
                getString(R.string.pref_passphrase_delimiter),
                getString(R.string.passphrase_default_delimiter)
        )
        val cap = prefs().getBoolean(
                getString(R.string.pref_passphrase_force_cap),
                resources.getBoolean(R.bool.pref_default_passphrase_force_cap)
        )

        // choose random elements for the first n positions in the array
        Utility.shuffleN(ids, n)

        // map the ids to Strings
        val selectionArgs = ids.take(n).map { it.toString() }.toTypedArray()

        // create the WHERE clause of the SQL statement
        val selection = List(n) { "${WordEntry._ID} = ?" }.joinToString(" OR ")

        val db = PreBuiltWordDBHelper(activity).readableDatabase
        val c = db.query(
                WordEntry.TABLE_NAME,
                arrayOf(WordEntry.COLUMN_WORD),
                selection,
                selectionArgs, null, null, null
        )

        // put the data in the cursor into an array so that it may be joined later
        if (!c.moveToFirst()) {
            Log.e(javaClass.simpleName, "Database Error")
            return null
        }

        var passphraseList = (1..c.count).map {
            val s = c.getString(0)!!
            c.moveToNext()
            s
        }

        c.close()
        db.close()

        // capitalise
        if (cap) passphraseList = passphraseList.map {
            val chars = it.toList()
            val first = chars[0].toUpperCase()
            (listOf<Char>(first) + chars.subList(1, chars.size)).toString()
        }

        return passphraseList.joinToString(delim)
    }


    // Fetch the words in the background
    inner class FetchWordListTask: AsyncTask<Pair<Int, Int>, Void, IntArray>() {
        override fun doInBackground(vararg params: Pair<Int, Int>): IntArray {
            val columns = arrayOf(WordEntry._ID)
            val selection = "${WordEntry.COLUMN_LEN} >= ? AND ${WordEntry.COLUMN_LEN} <= ?"
            val selectionArgs = arrayOf(params[0].first.toString(), params[0].second.toString())

            val db = PreBuiltWordDBHelper(activity).readableDatabase

            val c = db.query(
                    WordEntry.TABLE_NAME,
                    columns,
                    selection,
                    selectionArgs, null, null, null
            )

            if (!c.moveToFirst()) {
                Log.e(javaClass.simpleName, "Database Error")
                return intArrayOf()
            }

            val ids = (1..c.count).map {
                val i = c.getInt(0)
                c.moveToNext()
                i
            }

            c.close()
            db.close()
            return ids.toIntArray()
        }

        override fun onPostExecute(result: IntArray) {
            mWordIdsReady = true
            mWordIds = result
        }
    }

    private fun prefs(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
}
