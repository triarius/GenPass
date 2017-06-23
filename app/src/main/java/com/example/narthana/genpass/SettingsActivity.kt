package com.example.narthana.genpass

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem

/**
 * Created by narthana on 28/12/16.
 */

class SettingsActivity : AppCompatActivity() {
    private val PASSWORD_PREF_FRAGMENT_TAG = "password_pref_fragment"
    private val PASSPHRASE_PREF_FRAGMENT_TAG = "passphrase_pref_fragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set the content
        setContentView(R.layout.activity_settings)

        // set the action bar
        val toolbar = findViewById(R.id.toolbar_settings) as Toolbar
        setSupportActionBar(toolbar)

        // action bar back button
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
        //        else Log.d(this.getClass().getSimpleName(), "action bar could not be set up");


        // Display the fragment as the main content.
        if (savedInstanceState == null)
            fragmentManager.beginTransaction()
                    .replace(R.id.settings_frame, SettingsFragment())
                    .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // returns to previous pane, should be as if back button was pressed
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
