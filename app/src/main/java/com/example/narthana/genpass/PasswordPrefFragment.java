package com.example.narthana.genpass;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by narthana on 28/12/16.
 */

public class PasswordPrefFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_password);
    }
}
