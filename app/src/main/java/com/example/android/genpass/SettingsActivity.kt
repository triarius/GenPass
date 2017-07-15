package com.example.android.genpass

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.app_bar_settings.*

/**
 * Created by narthana on 28/12/16.
 */

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set the content
        setContentView(R.layout.activity_settings)

        // set the action bar
        setSupportActionBar(toolbar_settings)

        // action bar back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Display the fragment as the main content.
        if (savedInstanceState == null) fragmentManager
                .beginTransaction()
                .replace(R.id.settings_frame, SettingsFragment())
                .commit()
    }

    // returns to previous pane, should be as if back button was pressed
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> consume { finish() }
        else -> super.onOptionsItemSelected(item)
    }
}
