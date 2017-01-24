package com.example.narthana.genpass;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import static com.example.narthana.genpass.R.xml.prefs;

/**
 * Created by narthana on 28/12/16.
 */

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String SPACE_STRING = "{SPACE}";

    public static final int INT = 0;
    public static final int STRING = 1;
    @IntDef({INT, STRING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type{};

    private Map<String, ResIdWithType> mPrefKeyToId;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(prefs);

        mPrefKeyToId = new HashMap<>();

        // Just add a preference's keyId, defaultValueId and type to there three arrays to get it
        // it to have its values displayed as the summary
        final int[] prefIds = new int[] {
                R.string.pref_password_length_key,
                R.string.pref_passphrase_num_words,
                R.string.pref_passphrase_delimiter,
                R.string.pref_passphrase_min_word_length,
                R.string.pref_passphrase_max_word_length
        };
        final int[] defaultIds = new int[] {
                R.integer.pref_default_password_length,
                R.integer.pref_default_passphrase_num_words,
                R.string.passphrase_default_delimiter,
                R.integer.passpharase_default_min_word_length,
                R.integer.passpharase_default_max_word_length
        };
        final @Type int[] types = new int[] {
                INT,
                INT,
                STRING,
                INT,
                INT
        };

        for (int i = 0; i < prefIds.length; ++i)
            mPrefKeyToId.put(
                    getString(prefIds[i]),
                    new ResIdWithType(defaultIds[i], types[i])
            );
    }

    @Override
    public void onResume()
    {
        super.onResume();

        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // Set the summary values initially
        for (Map.Entry<String, ResIdWithType> entry : mPrefKeyToId.entrySet())
            setPrefSummaryToValue(
                    prefs,
                    entry.getKey(),
                    entry.getValue().defaultId,
                    entry.getValue().type
            );

        // register the listener
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        ResIdWithType value = mPrefKeyToId.get(key);
        if (value != null)
            setPrefSummaryToValue(sharedPreferences, key, value.defaultId, value.type);
    }

    private void setPrefSummaryToValue(SharedPreferences prefs, String key,
                                       int defaultValueId, @Type int type)
    {
        String summary;
        switch (type)
        {
            case INT:
                int intPref = prefs.getInt(
                        key,
                        getResources().getInteger(defaultValueId)
                );
                summary = String.valueOf(intPref);
                break;
            case STRING:
                summary = prefs.getString(
                        key,
                        getString(defaultValueId)
                );
                if (summary.equals(" ")) summary = SPACE_STRING;
                break;
            default:
                // AN ERROR HAS OCCURRED
                summary = null;
        }
        findPreference(key).setSummary(summary);
    }

    private static class ResIdWithType
    {
        final int defaultId;
        final @Type int type;

        ResIdWithType(int defaultId, @Type int type)
        {
            this.defaultId = defaultId;
            this.type = type;
        }
    }
}
