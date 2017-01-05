package com.example.narthana.genpass;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

/**
 * Created by narthana on 28/12/16.
 */

public class SettingsActivity extends AppCompatActivity
{
    private final String PASSWORD_PREF_FRAGMENT_TAG = "password_pref_fragment";
    private final String PASSPHRASE_PREF_FRAGMENT_TAG = "passphrase_pref_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
        else Log.d(this.getClass().getSimpleName(), "action bar could not be set up");


        // Display the fragment as the main content.
        if (savedInstanceState == null) getFragmentManager().beginTransaction()
                .replace(R.id.settings_frame, new PasswordPrefFragment())
                .commit();
    }
}
