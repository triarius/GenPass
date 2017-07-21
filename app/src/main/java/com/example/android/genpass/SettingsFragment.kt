package com.example.android.genpass

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import com.example.android.genpass.R.xml.prefs

/**
 * Created by narthana on 28/12/16.
 */

class SettingsFragment: PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var prefKeyToId: Map<String, DefaultIDWithType>

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
                R.string.pref_passphrase_max_word_length,
                R.string.pref_passphrase_mandatory_numerals,
                R.string.pref_passphrase_mandatory_symbols
        )
        val defaultIds = listOf(
                IntDefaultID(R.integer.pref_default_password_length),
                IntDefaultID(R.integer.pref_default_passphrase_num_words),
                StringDefaultID(R.string.pref_default_passphrase_delimiter),
                IntDefaultID(R.integer.pref_default_passphrase_min_word_length),
                IntDefaultID(R.integer.pref_default_passphrase_max_word_length),
                IntDefaultID(R.integer.pref_default_passphrase_mandatory_numerals),
                IntDefaultID(R.integer.pref_default_passphrase_mandatory_symbols)
        )

        prefKeyToId = prefIds.map(this::getString).zip(defaultIds).toMap()
    }

    override fun onResume() {
        super.onResume()
        with (preferenceScreen.sharedPreferences) {
            // Set the summary values initially
            prefKeyToId.forEach { setPrefSummaryTo(this, it) }

            // register the listener
            registerOnSharedPreferenceChangeListener(this@SettingsFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        prefKeyToId.forEach { setPrefSummaryTo(prefs, it) }
    }

    private fun setPrefSummaryTo(prefs: SharedPreferences,
                                 entry: Map.Entry<String, DefaultIDWithType>) {
        findPreference(entry.key).summary = entry.run {
            when (entry.value) {
                is IntDefaultID -> prefs.getInt(key, resources.getInteger(value.id)).toString()
                is StringDefaultID -> prefs.getString(key, getString(value.id)).run {
                    if (this == " ") SPACE_STRING else this
                }
            }
        }
    }

    companion object {
        private const val SPACE_STRING = "{SPACE}"
    }
}

sealed class DefaultIDWithType(val id: Int)
class IntDefaultID(id: Int): DefaultIDWithType(id)
class StringDefaultID(id: Int): DefaultIDWithType(id)