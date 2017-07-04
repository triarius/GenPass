package com.example.android.genpass

import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.genpass.data.PreBuiltWordDBHelper
import com.example.android.genpass.data.WordContract.WordEntry
import kotlinx.android.synthetic.main.fragment_passphrase.*
import java.security.SecureRandom

/**
 * Created by narthana on 22/10/16.
 */

class PassphraseFragment: Fragment() {
    private var mWordIds: WordListResult = WordListLoading
    private var mPassphraseCopyable = false
    private var mPassphrase: String? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val minWordLen = getIntPref(
                getString(R.string.pref_passphrase_min_word_length),
                resources.getInteger(R.integer.pref_default_passphrase_min_word_length)
        )
        val maxWordLen = getIntPref(
                getString(R.string.pref_passphrase_max_word_length),
                resources.getInteger(R.integer.pref_default_passpharse_max_word_length)
        )
        FetchWordListTask().execute(Pair(minWordLen, maxWordLen))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.run {
            mPassphraseCopyable = getBoolean(COPYABLE_TAG)
            mPassphrase = getString(PASSPHRASE_TAG)
            mWordIds = getIntArray(WORDS_TAG)?.run {
                WordList(
                        this,
                        getInt(MIN_WORD_LEN_TAG),
                        getInt(MAX_WORD_LEN_TAG)
                )
            } ?: WordListError
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.fragment_passphrase, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPassphrase?.run { textview_passphrase.text = this }

        // set click listeners
        button_generate_passphrase.setOnClickListener {
            when (mWordIds) {
                is WordList -> {
                    mPassphrase = createPhrase(mWordIds as WordList)
                    textview_passphrase.text = mPassphrase
                    mPassphraseCopyable = true
                }
                is WordListLoading -> {
                    Snackbar.make(view, R.string.dict_load_snack, Snackbar.LENGTH_SHORT).show()
                }
                is WordListError -> {
                    Snackbar.make(view, R.string.empty_charset, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        textview_passphrase.setOnClickListener {
            if (mPassphraseCopyable) {
                val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE)
                        as ClipboardManager
                val clip = ClipData.newPlainText(
                        getString(R.string.clipboard_text),
                        textview_passphrase.text
                )
                clipboard.primaryClip = clip
                Snackbar.make(view, R.string.copy_msg, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val minWordLen = getIntPref(
                getString(R.string.pref_passphrase_min_word_length),
                resources.getInteger(R.integer.pref_default_passphrase_min_word_length)
        )
        val maxWordLen = getIntPref(
                getString(R.string.pref_passphrase_max_word_length),
                resources.getInteger(R.integer.pref_default_passpharse_max_word_length)
        )

        val wordList = mWordIds
        when (wordList) {
            is WordList ->
                if (minWordLen != wordList.minWordLen || maxWordLen != wordList.maxWordLen)
                    FetchWordListTask().execute(Pair(minWordLen, maxWordLen))
            else -> FetchWordListTask().execute(Pair(minWordLen, maxWordLen))
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        with (savedInstanceState) {
            mPassphrase?.run { putString(PASSPHRASE_TAG, this) }
            val wordList = mWordIds
            if (wordList is WordList) {
                putIntArray(WORDS_TAG, wordList.array)
                putInt(MIN_WORD_LEN_TAG, wordList.minWordLen)
                putInt(MAX_WORD_LEN_TAG, wordList.maxWordLen)
            }
            putBoolean(COPYABLE_TAG, mPassphraseCopyable)
        }
    }

    private fun createPhrase(wordIds: WordList): String? {
        val numNum = getIntPref(
                getString(R.string.pref_passphrase_mandatory_numerals),
                resources.getInteger(R.integer.pref_default_passpharse_mandatory_numerals)
        )
        val numSymb = getIntPref(
                getString(R.string.pref_passphrase_mandatory_symbols),
                resources.getInteger(R.integer.pref_default_passpharse_mandatory_symbols)
        )
        val n = getIntPref(
                getString(R.string.pref_passphrase_num_words),
                resources.getInteger(R.integer.pref_default_passphrase_num_words)
        ) - if (numNum + numSymb > 0) 1 else 0
        val delim = getStringPref(
                getString(R.string.pref_passphrase_delimiter),
                getString(R.string.pref_default_passphrase_delimiter)
        )
        val cap = getBooleanPref(
                getString(R.string.pref_passphrase_force_cap),
                resources.getBoolean(R.bool.pref_default_passphrase_force_cap)
        )

        // look up n random words in the database
        val db = PreBuiltWordDBHelper(activity).readableDatabase
        val cursor = db.query(
                WordEntry.TABLE_NAME,
                arrayOf(WordEntry.COLUMN_WORD),
                List(n) { "${WordEntry._ID} = ?" }.joinToString(" OR "),
                wordIds.array.randomN(n, random).map(Int::toString).toTypedArray(),
                null, null, null
        )

        // put the data in the cursor into an array so that it may be joined later
        if (!cursor.moveToFirst()) {
            Log.e(javaClass.simpleName, "Database Error")
            return null
        }

        var passphraseList = (1..cursor.count).map {
            val s = cursor.getString(0)!!
            cursor.moveToNext()
            s
        }.toMutableList()

        cursor.close()
        db.close()

        // capitalise
        if (cap) passphraseList = passphraseList.map {
            val chars = it.toMutableList()
            chars[0] = chars[0].toUpperCase()
            chars.joinToString("")
        }.toMutableList()

        if (numNum + numSymb > 0) passphraseList.add(randomString(numNum, numSymb))

        return passphraseList.joinToString(delim)
    }

    private fun randomString(numNum: Int, numSymb: Int): String {
        val charsetMap = (activity as MainActivity).charsetMap
        val numerals = charsetMap[getString(R.string.pref_password_numerals_charset_key)] ?: ""
        val symbols = charsetMap[getString(R.string.pref_password_symbols_charset_key)] ?: ""
        val charsets = (1 .. numNum).map { setOf(numerals) } + (1..numSymb).map { setOf(symbols) }
        return String(charsets.randomString(random).toCharArray().shuffle(random))
    }

    // Fetch the words in the background
    private inner class FetchWordListTask(): AsyncTask<Pair<Int, Int>, Void, WordListResult>() {
        override fun doInBackground(vararg params: Pair<Int, Int>): WordListResult {
            val db = PreBuiltWordDBHelper(activity).readableDatabase

            val cursor = db.query(
                    WordEntry.TABLE_NAME,
                    arrayOf(WordEntry._ID),
                    "${WordEntry.COLUMN_LEN} >= ? AND ${WordEntry.COLUMN_LEN} <= ?",
                    params[0].toList().map(Int::toString).toTypedArray(),
                    null, null, null
            )

            if (!cursor.moveToFirst()) {
                Log.e(javaClass.simpleName, "Database Error")
                cursor.close()
                db.close()
                return WordListError
            }

            val ids = (1..cursor.count).map {
                val i = cursor.getInt(0)
                cursor.moveToNext()
                i
            }

            cursor.close()
            db.close()
            val (min, max) = params[0]
            return WordList(ids.toIntArray(), min, max)
        }

        override fun onPreExecute() { mWordIds = WordListLoading }

        override fun onPostExecute(result: WordListResult) { mWordIds = result }
    }

    companion object {
        private const val WORDS_TAG = "words"
        private const val PASSPHRASE_TAG = "passphrase"
        private const val COPYABLE_TAG = "copyable"
        private const val MIN_WORD_LEN_TAG = "minwordlen"
        private const val MAX_WORD_LEN_TAG = "maxwordlen"
        private val random = SecureRandom()
    }
}