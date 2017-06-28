package com.example.narthana.genpass

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import com.example.narthana.genpass.R.xml.prefs

/**
 * Created by narthana on 28/12/16.
 */

class SettingsFragment: PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val mPrefKeyToId: Map<String, Pair<Int, Type>>

    init {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(prefs)
    }

    override fun onResume() {
        super.onResume()

        // Set the summary values initially
        mPrefKeyToId.forEach {
            setPrefSummaryTo(preferenceScreen.sharedPreferences, it.toPair())
        }

        // register the listener
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val value = mPrefKeyToId[key]
        if (value != null) setPrefSummaryTo(sharedPreferences, Pair(key, value))
    }

    private fun setPrefSummaryTo(prefs: SharedPreferences, entry: Pair<String, Pair<Int, Type>>) {
        findPreference(entry.first).summary = when (entry.second.second) {
            Type.INT -> prefs.getInt(
                    entry.first,
                    resources.getInteger(entry.second.first)
            ).toString()
            Type.STRING -> {
                val str = prefs.getString(entry.first, getString(entry.second.first))
                if (str == " ") SPACE_STRING else str
            }
            else -> null
        }
    }

    enum class Type { INT, STRING }

    companion object {
        val SPACE_STRING = "{SPACE}"
    }
}
