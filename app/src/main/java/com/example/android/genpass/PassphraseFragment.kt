package com.example.android.genpass

import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
    private var wordIds: WordListResult = WordListLoading
    private lateinit var passphrase: Pass
    private lateinit var passphraseError: PassphraseError

    private val fetchWords: FetchWordListTask
        get() = FetchWordListTask(
                PreBuiltWordDBHelper(context).readableDatabase,
                { wordIds = WordListLoading },
                { wordIds = it }
        )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        passphraseError = PassphraseError()

        val minWordLen = getIntPref(
                getString(R.string.pref_passphrase_min_word_length),
                resources.getInteger(R.integer.pref_default_passphrase_min_word_length)
        )
        val maxWordLen = getIntPref(
                getString(R.string.pref_passphrase_max_word_length),
                resources.getInteger(R.integer.pref_default_passpharse_max_word_length)
        )
        fetchWords.execute(Pair(minWordLen, maxWordLen))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.apply {
            passphrase = if (getBoolean(COPYABLE_TAG)) ValidPass(getString(PASSPHRASE_TAG))
                         else InvalidPass(getString(PASSPHRASE_TAG))
            wordIds = getIntArray(WORDS_TAG)?.run {
                WordList(this, getInt(MIN_WORD_LEN_TAG), getInt(MAX_WORD_LEN_TAG))
            } ?: WordListError
        } ?: run {
            passphrase = DefaultPassphrase()
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.fragment_passphrase, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textview_passphrase.text = passphrase.text

        // set click listeners
        button_generate_passphrase.setOnClickListener {
            when (wordIds) {
                is WordList -> {
                    passphrase = createPhrase(wordIds as WordList)
                    textview_passphrase.text = passphrase.text
                }
                is WordListLoading ->
                    Snackbar.make(view, R.string.dict_load_snack, Snackbar.LENGTH_SHORT).show()
                is WordListError ->
                    Snackbar.make(view, R.string.empty_charset, Snackbar.LENGTH_SHORT).show()
            }
        }

        textview_passphrase.setOnClickListener {
            if (passphrase is CopyablePass) {
                getSysService<ClipboardManager>(Context.CLIPBOARD_SERVICE).primaryClip =
                        ClipData.newPlainText(getString(R.string.clipboard_text), passphrase.text)
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

        wordIds.apply { when (this) {
            is WordList -> {
                if (minWordLen != this.minWordLen || maxWordLen != this.maxWordLen)
                    fetchWords.execute(Pair(minWordLen, maxWordLen))
            }
            else -> fetchWords.execute(Pair(minWordLen, maxWordLen))
        }}
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        with (savedInstanceState) {
            if (wordIds is WordList) with (wordIds as WordList) {
                putIntArray(WORDS_TAG, array)
                putInt(MIN_WORD_LEN_TAG, minWordLen)
                putInt(MAX_WORD_LEN_TAG, maxWordLen)
            }
            putString(PASSPHRASE_TAG, passphrase.text)
            putBoolean(COPYABLE_TAG, passphrase is CopyablePass)
        }
    }

    private fun createPhrase(wordIds: WordList): Pass {
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
        )
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
            return passphraseError
        }

        var passphraseList = (1..cursor.count).map {
            val s = cursor.getString(0)!!
            cursor.moveToNext()
            s
        } as MutableList<String>

        cursor.close()
        db.close()

        // capitalise
        if (cap) passphraseList = passphraseList.map {
            it.toCharArray().apply { this[0] = this[0].toUpperCase() }.joinToString(EMPTY_STRING)
        } as MutableList<String>

        if (numNum + numSymb > 0) passphraseList.add(randomString(numNum, numSymb))

        return ValidPass(passphraseList.joinToString(delim))
    }

    private fun randomString(numNum: Int, numSymb: Int): String {
        val numerals = (activity as MainActivity)
                .getCharSet(R.string.pref_password_numerals_charset_key)
                .joinToString(EMPTY_STRING)
        val symbols = (activity as MainActivity)
                .getCharSet(R.string.pref_password_symbols_charset_key)
                .joinToString(EMPTY_STRING)
        return ((1 .. numNum).map { numerals } + (1..numSymb).map { symbols })
                .randomString(random)
                .toCharArray()
                .shuffle(random)
                .joinToString(EMPTY_STRING)
    }

    // Fetch the words in the background
    private class FetchWordListTask(
            val db: SQLiteDatabase,
            val preExecute: () -> Unit,
            val postExecute: (WordListResult) -> Unit
    ): AsyncTask<Pair<Int, Int>, Void, WordListResult>() {
        override fun doInBackground(vararg params: Pair<Int, Int>): WordListResult {
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

        override fun onPreExecute() = preExecute()
        override fun onPostExecute(result: WordListResult) = postExecute(result)
    }

    internal inner open class LookupPass(resId: Int): UncopyablePass() {
        override val text = getString(resId)
    }
    internal inner class DefaultPassphrase: LookupPass(R.string.default_passphrase_text)
    internal inner class PassphraseError: LookupPass(R.string.passphrase_error)

    companion object {
        private const val EMPTY_STRING = ""
        private const val WORDS_TAG = "words"
        private const val PASSPHRASE_TAG = "passphrase"
        private const val COPYABLE_TAG = "copyable"
        private const val MIN_WORD_LEN_TAG = "minwordlen"
        private const val MAX_WORD_LEN_TAG = "maxwordlen"
        private val random = SecureRandom()
    }
}