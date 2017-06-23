package com.example.narthana.genpass

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import com.example.narthana.genpass.R.xml.prefs
import java.util.*

/**
 * Created by narthana on 28/12/16.
 */

class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var mPrefKeyToId: MutableMap<String, Pair<Int, Type>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(prefs)

        mPrefKeyToId = HashMap<String, Pair<Int, Type>>()

        // Just add a preference's keyId, defaultValueId and type to there three arrays to get it
        // it to have its values displayed as the summary
        val prefIds = intArrayOf(R.string.pref_password_length_key, R.string.pref_passphrase_num_words, R.string.pref_passphrase_delimiter, R.string.pref_passphrase_min_word_length, R.string.pref_passphrase_max_word_length)
        val defaultIds = intArrayOf(R.integer.pref_default_password_length, R.integer.pref_default_passphrase_num_words, R.string.passphrase_default_delimiter, R.integer.passpharase_default_min_word_length, R.integer.passpharase_default_max_word_length)
        val types: Array<Type> = arrayOf(Type.INT, Type.INT, Type.STRING, Type.INT, Type.INT)

        for (i in prefIds.indices)
            mPrefKeyToId!!.put(
                    getString(prefIds[i]),
                    Pair(defaultIds[i], types[i])
            )
    }

    override fun onResume() {
        super.onResume()

        val prefs = preferenceScreen.sharedPreferences

        // Set the summary values initially
        for ((key, value) in mPrefKeyToId!!)
            setPrefSummaryToValue(
                    prefs,
                    key,
                    value.first,
                    value.second
            )

        // register the listener
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val value = mPrefKeyToId!![key]
        if (value != null)
            setPrefSummaryToValue(sharedPreferences, key, value.first, value.second)
    }

    private fun setPrefSummaryToValue(prefs: SharedPreferences, key: String,
                                      defaultValueId: Int, type: Type) {
        findPreference(key).summary = when (type) {
            Type.INT -> prefs.getInt(key, resources.getInteger(defaultValueId)).toString()
            Type.STRING -> {
                val str = prefs.getString(key, getString(defaultValueId))
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
