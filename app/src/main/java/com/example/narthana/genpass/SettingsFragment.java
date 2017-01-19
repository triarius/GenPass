package com.example.narthana.genpass;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by narthana on 28/12/16.
 */

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final int INT = 0;
    public static final int STRING = 1;
    @IntDef({INT, STRING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type{};

    private Map<String, PairOfIds> mPrefKeyToId;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        mPrefKeyToId = new HashMap<>();
        final int[] prefIds = new int[] {
                R.string.pref_password_length_key,
                R.string.pref_passphrase_num_words,
                R.string.pref_passphrase_delimiter
        };
        final int[] defaultIds = new int[] {
                R.integer.pref_default_password_length,
                R.integer.pref_default_passphrase_num_words,
                R.string.passphrase_default_delimiter
        };
        final @Type int[] types = new int[] {
                INT,
                INT,
                STRING
        };

        for (int i = 0; i < prefIds.length; ++i)
            mPrefKeyToId.put(
                    getString(prefIds[i]),
                    new PairOfIds(defaultIds[i], types[i])
            );
    }

    @Override
    public void onResume()
    {
        super.onResume();

        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // Set the summary values initially
        for (Map.Entry<String, PairOfIds> entry : mPrefKeyToId.entrySet())
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
        PairOfIds value = mPrefKeyToId.get(key);
        if (value != null)
            setPrefSummaryToValue(sharedPreferences, key, value.defaultId, value.type);
    }

    private void setPrefSummaryToValue(SharedPreferences prefs, String key,
                                       int defaultValueId, @Type int type)
    {
        Object stuff = null;
        switch (type)
        {
            case INT:
                stuff = prefs.getInt(
                        key,
                        getResources().getInteger(defaultValueId)
                );
                break;
            case STRING:
                stuff = prefs.getString(
                        key,
                        getString(defaultValueId)
                );
                break;
        }
        findPreference(key).setSummary(String.valueOf(stuff));
    }

    private static class PairOfIds
    {
        final int defaultId;
        final @Type int type;

        PairOfIds(int defaultId, @Type int type)
        {
            this.defaultId = defaultId;
            this.type = type;
        }
    }
}
