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
    private var mPasswordCopyable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.run { mPasswordCopyable = getBoolean(COPYABLE_TAG) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.fragment_password, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set text
        savedInstanceState?.run { textview_password.text = getCharSequence(PASSWORD_TAG) }

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
            val numChars: Int = getIntPref(
                    getString(R.string.pref_password_length_key),
                    resources.getInteger(R.integer.pref_default_password_length)
            )
            textview_password.text = newPassword(numChars, view)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) = with (outState) {
        super.onSaveInstanceState(this)
        putCharSequence(PASSWORD_TAG, textview_password.text)
        putBoolean(COPYABLE_TAG, mPasswordCopyable)
    }

    private fun newPassword(len: Int, rootView: View): String {
        if (len < 1) return passwordError(rootView, R.string.zero_length)

        // create charset to draw from
        val key = getString(R.string.pref_password_charset_key)
        val selectedCharsetKeys = getStringSetPref(
                key + getString(R.string.pref_password_charset_col_enabled),
                resources.getStringArray(R.array.pref_default_password_charset_enabled).toSet()
        )
        // collect the mandatory preferences into an array, and count them
        val mandCharsetKeys = getStringSetPref(
                key + getString(R.string.pref_password_charset_col_mandatory),
                resources.getStringArray(R.array.pref_default_password_charset_mandatory).toSet()
        )

        // the user has not checked any char subsets to add to the charset
        if (selectedCharsetKeys.isEmpty()) return passwordError(rootView, R.string.empty_charset)

        // TODO: prevent the UI from allowing this to occur
        if (mandCharsetKeys.size > len) return passwordError(rootView, R.string.too_many_mandates)

        // select the chars to be in the password
        val charsetMap = (activity as MainActivity).charsetMap
        val mandatoryCharsets = mandCharsetKeys.map { setOf(it) }
        val selectedCharset = selectedCharsetKeys.map { charsetMap[it] ?: "" }.toSet()
        val optionalCharsets = (mandCharsetKeys.size .. len).map { selectedCharset }
        val charsets = (mandatoryCharsets + optionalCharsets)

        val password = charsets.randomString(random).toCharArray()

        // shuffle the password so that the mandatory characters are in random positions
        return String(password.shuffle(random))
    }

    private fun passwordError(view: View, snackbarStringId: Int): String {
        Snackbar.make(view, getString(snackbarStringId), Snackbar.LENGTH_SHORT).show()
        mPasswordCopyable = false
        return getString(R.string.password_error)
    }

    companion object {
        private const val PASSWORD_TAG = "password"
        private const val COPYABLE_TAG = "copyable"
        private val random = SecureRandom()
    }
}