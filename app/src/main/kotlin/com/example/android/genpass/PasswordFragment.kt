package com.example.android.genpass

import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
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
    private lateinit var password: Pass
    private lateinit var passwordError: PasswordError

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        passwordError = PasswordError()
        password = savedInstanceState?.run {
            if (getBoolean(COPYABLE_TAG)) ValidPass(getString(PASSWORD_TAG))
            else InvalidPass(getString(PASSWORD_TAG))
        } ?: DefaultPassword()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.fragment_password, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textview_password.text = password.text

        // set listener to copy password
        textview_password.setOnClickListener {
            if (password is CopyablePass) {
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
            val numChars = getPrefFromId<Int>(
                    R.string.pref_password_charset_col_mandatory,
                    R.integer.pref_default_password_length
            )
            password = newPassword(numChars, view)
            textview_password.text = password.text
        }
    }

    override fun onSaveInstanceState(outState: Bundle) = with (outState) {
        super.onSaveInstanceState(this)
        putString(PASSWORD_TAG, password.text)
        putBoolean(COPYABLE_TAG, password is CopyablePass)
    }

    private fun newPassword(len: Int, rootView: View): Pass {
        if (len < 1) return pwErrHdlr(rootView, R.string.zero_length)

        // create charset to draw from
        val key = getString(R.string.pref_password_charset_key)
        val selectedKeys = getPref<Set<String>>(
                key + getString(R.string.pref_password_charset_col_enabled),
                R.array.pref_default_password_charset_enabled,
                SharedPreferences::getStringSet,
                stringArrayToSet
        )
        // collect the mandatory preferences into an array, and count them
        val mandatoryKeys = getPref<Set<String>>(
                key + getString(R.string.pref_password_charset_col_mandatory),
                R.array.pref_default_password_charset_mandatory,
                SharedPreferences::getStringSet,
                stringArrayToSet
        )
        // the user has not checked any char subsets to add to the charset
        if (selectedKeys.isEmpty()) return pwErrHdlr(rootView, R.string.empty_charset)

        // TODO: prevent the UI from allowing this to occur
        if (mandatoryKeys.size > len) return pwErrHdlr(rootView, R.string.too_many_mandates)

        // select the chars to be in the password
        val mandatoryCharsets = mandatoryKeys.map((activity as MainActivity)::getCharSetString)
        val optionalCharset = selectedKeys
                .map((activity as MainActivity)::getCharSetString)
                .joinToString(EMPTY_STRING)
        val optionalCharsets = (mandatoryKeys.size .. len).map { optionalCharset }
        return (mandatoryCharsets + optionalCharsets)
                .randomString(random)
                .toCharArray()
                .shuffle(random) // shuffle again so that the mandatory are in random positions
                .joinToString(EMPTY_STRING)
                .toPassword()
    }

    private fun String.toPassword() = ValidPass(this)

    private fun pwErrHdlr(view: View, snackbarStringId: Int): Pass {
        Snackbar.make(view, getString(snackbarStringId), Snackbar.LENGTH_SHORT).show()
        return passwordError
    }

    internal inner open class LookupPass(resId: Int): UncopyablePass() {
        override val text = getString(resId)
    }
    internal inner class DefaultPassword: LookupPass(R.string.default_password_text)
    internal inner class PasswordError: LookupPass(R.string.password_error)

    companion object {
        private const val EMPTY_STRING = ""
        private const val PASSWORD_TAG = "password"
        private const val COPYABLE_TAG = "copyable"
        private val random = SecureRandom()
    }
}