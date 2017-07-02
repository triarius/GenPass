package com.example.android.genpass

import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_password.*
import java.security.SecureRandom

/**
 * Created by narthana on 22/10/16.
 */

class PasswordFragment: Fragment() {
    private var mKeyToCharset: Map<String, String>? = null
    private var mDefaultCharsetKeys: Set<String>? = null
    private var mDefManCharsetKeys: Set<String>? = null

    private var mPasswordCopyable = false
    private var mPassText: String? = null

    override fun onAttach(context: Context) = with (resources) {
        super.onAttach(context)
        val charsetKeys = getStringArray(R.array.pref_password_charset_keys)
        val charsets = getStringArray(R.array.charsets)

        // create map charsetKeys -> charsets
        mKeyToCharset = charsetKeys.zip(charsets).associate { it }
        mDefaultCharsetKeys = getStringArray(R.array.pref_password_charset_default_enabled).toSet()
        mDefManCharsetKeys = getStringArray(R.array.pref_password_charset_default_mandatory).toSet()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPassText = savedInstanceState?.getString(PASSWORD_TAG)
        mPasswordCopyable = savedInstanceState?.getBoolean(COPYABLE_TAG) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, cntr: ViewGroup?, state: Bundle?): View
        = inflater.inflate(R.layout.fragment_password, cntr, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set texts
        mPassText?.run { textview_password.text = this }

        // set listener to copy password
        textview_password.setOnClickListener {
            if (mPasswordCopyable) {
                val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE)
                        as ClipboardManager
                val clip = ClipData.newPlainText(
                        getString(R.string.clipboard_text),
                        textview_password.text
                )
                clipboard.primaryClip = clip
                Snackbar.make(view, R.string.copy_msg, Snackbar.LENGTH_SHORT).show()
            }
        }

        // attach click listener to button
        button_generate_password.setOnClickListener {
            mPasswordCopyable = true
            mPassText = newPassword(numChars(), view)
            mPassText?.run { textview_password.text = this }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) = with (outState) {
        super.onSaveInstanceState(this)
        mPassText?.run { putString(PASSWORD_TAG, this) }
        putBoolean(COPYABLE_TAG, mPasswordCopyable)
    }

    private fun newPassword(len: Int, rootView: View): String? {
        if (len < 1) {
            Snackbar.make(rootView, R.string.zero_length, Snackbar.LENGTH_SHORT).show()
            return null
        }

        // create charset to draw from
        val key = getString(R.string.pref_password_charset_key)
        val selectedCharsetKeys = getStringSet(
                key + getString(R.string.pref_password_charset_col_enabled),
                mDefaultCharsetKeys
        )
        // collect the mandatory preferences into an array, and count them
        val mandatoryCharsetKeys = getStringSet(
                key + getString(R.string.pref_password_charset_col_mandatory),
                mDefManCharsetKeys
        )

        // the user has not checked any char subsets to add to the charset
        if (selectedCharsetKeys == null || mandatoryCharsetKeys == null
                || selectedCharsetKeys.isEmpty()) {
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

        val password = charSets.map { it!![random.nextInt(it.length)] }.toCharArray()

        // shuffle the password so that the mandatory characters are in random positions
        shuffle(password, random)

        return String(password)
    }

    private fun numChars(): Int = getInt(
            getString(R.string.pref_password_length_key),
            resources.getInteger(R.integer.pref_default_password_length)
    )

    companion object {
        private const val PASSWORD_TAG = "password"
        private const val COPYABLE_TAG = "copyable"
        private val random = SecureRandom()
    }
}