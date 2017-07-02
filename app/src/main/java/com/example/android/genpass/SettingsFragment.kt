package com.example.android.genpass

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import com.example.android.genpass.R.xml.prefs

/**
 * Created by narthana on 28/12/16.
 */

class SettingsFragment: PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var mPrefKeyToId: Map<String, Pair<Int, Type>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(prefs)

        // Just add a preference's keyId, defaultValueId and type to these three arrays to get it
        // it to have its values displayed as the summary
        val prefIds = intArrayOf(
                R.string.pref_password_length_key,
                R.string.pref_passphrase_num_words,
                R.string.pref_passphrase_delimiter,
                R.string.pref_passphrase_min_word_length,
                R.string.pref_passphrase_max_word_length
        )
        val defaultIds = intArrayOf(
                R.integer.pref_default_password_length,
                R.integer.pref_default_passphrase_num_words,
                R.string.passphrase_default_delimiter,
                R.integer.passpharase_default_min_word_length,
                R.integer.passpharase_default_max_word_length
        )
        val types: Array<Type> = arrayOf(Type.INT, Type.INT, Type.STRING, Type.INT, Type.INT)

        mPrefKeyToId = prefIds.map(this::getString).zip(defaultIds.zip(types)).toMap()
    }

    override fun onResume() {
        super.onResume()
        with (preferenceScreen.sharedPreferences) {
            // Set the summary values initially
            mPrefKeyToId?.forEach { setPrefSummaryTo(this, it.toPair()) }

            // register the listener
            registerOnSharedPreferenceChangeListener(this@SettingsFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        mPrefKeyToId?.get(key)?.let { setPrefSummaryTo(sharedPreferences, Pair(key, it)) }
    }

    private fun setPrefSummaryTo(prefs: SharedPreferences, entry: Pair<String, Pair<Int, Type>>) {
        findPreference(entry.first).summary = when (entry.second.second) {
            Type.INT -> prefs.getInt(
                    entry.first,
                    resources.getInteger(entry.second.first)
            ).toString()
            Type.STRING -> prefs.getString(entry.first, getString(entry.second.first)).run {
                if (this == " ") SPACE_STRING else this
            }
        }
    }

    enum class Type { INT, STRING }

    companion object {
        private const val SPACE_STRING = "{SPACE}"
    }
}