package com.example.narthana.genpass

import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import java.security.SecureRandom

/**
 * Created by narthana on 22/10/16.
 */

class PasswordFragment: Fragment() {
    private val mRandom = SecureRandom()

    private var mKeyToCharset: Map<String, String>? = null
    private var mDefaultCharsetKeys: Set<String>? = null
    private var mDefManCharsetKeys: Set<String>? = null

    private var mPasswordCopyable = false
    private var mPassText: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val charsetKeys = resources.getStringArray(R.array.pref_password_charset_keys)
        val charsets = resources.getStringArray(R.array.charsets)

        // create map charsetKeys -> charsets
        mKeyToCharset = charsetKeys.zip(charsets).associate { it }
        mDefaultCharsetKeys = resources.getStringArray(R.array.pref_password_charset_default_enabled).toSet()
        mDefManCharsetKeys = resources.getStringArray(R.array.pref_password_charset_default_mandatory).toSet()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPassText = savedInstanceState?.getString(PASSWORD_TAG)
        mPasswordCopyable = savedInstanceState?.getBoolean(COPYABLE_TAG) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_password, container, false)
        val tvPass = rootView.findViewById(R.id.password_textview) as TextView
        val btnGenerate = rootView.findViewById(R.id.button_generate_password) as Button

        // Set texts
        if (mPassText != null) tvPass.text = mPassText

        // set listener to copy password
        tvPass.setOnClickListener {
            if (mPasswordCopyable) {
                val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                        getString(R.string.clipboard_text),
                        tvPass.text
                )
                clipboard.primaryClip = clip
                Snackbar.make(rootView, R.string.copy_msg, Snackbar.LENGTH_SHORT).show()
            }
        }

        // attach click listener to button
        btnGenerate.setOnClickListener {
            mPasswordCopyable = true
            mPassText = newPassword(numChars(), rootView)
            if (mPassText != null) tvPass.text = mPassText
        }

        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mPassText != null) outState.putString(PASSWORD_TAG, mPassText)
        outState.putBoolean(COPYABLE_TAG, mPasswordCopyable)
    }

    private fun newPassword(len: Int, rootView: View): String? {
        if (len < 1) {
            Snackbar.make(rootView, R.string.zero_length, Snackbar.LENGTH_SHORT).show()
            return null
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

        // create charset to draw from
        val key = getString(R.string.pref_password_charset_key)
        val selectedCharsetKeys = prefs.getStringSet(
                key + getString(R.string.pref_password_charset_col_enabled),
                mDefaultCharsetKeys
        )!!
        // collect the mandatory preferences into an array, and count them
        val mandatoryCharsetKeys = prefs.getStringSet(
                key + getString(R.string.pref_password_charset_col_mandatory),
                mDefManCharsetKeys
        )!!

        // the user has not checked any char subsets to add to the charset
        if (selectedCharsetKeys.size == 0) {
            Snackbar.make(rootView, R.string.empty_charset, Snackbar.LENGTH_SHORT).show()
            return null
        }

        // TODO: prevent the UI from allowing this to occur
        if (mandatoryCharsetKeys.size > len) {
            Snackbar.make(rootView, R.string.too_many_mandates, Snackbar.LENGTH_SHORT).show()
            return null
        }

        // select the chars to be in the password
        val mandatoryCharsets = mandatoryCharsetKeys.map { mKeyToCharset!![it] }
        val selectedCharset = selectedCharsetKeys.map { mKeyToCharset!![it] }.joinToString("")
        val optionalCharsets = (mandatoryCharsetKeys.size .. len).map { selectedCharset }
        val charSets = mandatoryCharsets + optionalCharsets

        val password = charSets.map { it!![mRandom.nextInt(it.length)] }.toCharArray()

        // shuffle the password so that the mandatory characters are in random positions
        Utility.shuffle(password)

        return String(password)
    }

    private fun numChars(): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        return prefs.getInt(
                getString(R.string.pref_password_length_key),
                resources.getInteger(R.integer.pref_default_password_length)
        )
    }

    companion object {
        private val PASSWORD_TAG = "password"
        private val COPYABLE_TAG = "copyable"
    }
}
