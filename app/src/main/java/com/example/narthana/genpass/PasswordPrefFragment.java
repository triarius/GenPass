package com.example.narthana.genpass;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by narthana on 28/12/16.
 */

public class PasswordPrefFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private int DEFAULT_PASS_LENGTH;
    private String PREF_PASSWORD_LENGTH_KEY;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_password);
        DEFAULT_PASS_LENGTH = getResources().getInteger(R.integer.pref_default_password_length);
        PREF_PASSWORD_LENGTH_KEY = getString(R.string.pref_password_length_key);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        setIntPrefSummaryToValue(PREF_PASSWORD_LENGTH_KEY, DEFAULT_PASS_LENGTH);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
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
        if (key.equals(PREF_PASSWORD_LENGTH_KEY))
            setIntPrefSummaryToValue(key, DEFAULT_PASS_LENGTH);
    }

    private void setIntPrefSummaryToValue(String key, int defaultValue)
    {
        findPreference(key).setSummary(
                String.valueOf(getPreferenceScreen()
                        .getSharedPreferences()
                        .getInt(key, defaultValue))
        );
    }
}
