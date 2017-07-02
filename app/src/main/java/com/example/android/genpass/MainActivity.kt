package com.example.android.genpass

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var mNavMenuItemId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // pick layout
        setContentView(R.layout.activity_main)

        // create action bar
        setSupportActionBar(toolbar)

        // create nav Drawer
        val toggle = ActionBarDrawerToggle( this, drawer_layout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // set listener to open drawer
        nav_view.setNavigationItemSelectedListener(this)

        // if a new run or if restoring from prev state
        mNavMenuItemId = savedInstanceState?.getInt(NAV_MENU_ITEM_TAG) ?: run {
            // open password fragment
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, PasswordFragment(), PASSWORD_FRAGMENT_TAG)
                    .addToBackStack(PASSWORD_FRAGMENT_TAG)
                    .commit()

            // assign menu item id for later use in the nav bar
            R.id.nav_password
        }

        // set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false)
    }

    override fun onResume() {
        super.onResume()
        // set the selected item in the nav bar
        nav_view.setCheckedItem(mNavMenuItemId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(NAV_MENU_ITEM_TAG, mNavMenuItemId)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)
        else finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Gather fragments
        val pwf = (fragmentManager.findFragmentByTag(PASSWORD_FRAGMENT_TAG)
                   ?: PasswordFragment()) as PasswordFragment
        val ppf = (fragmentManager.findFragmentByTag(PASSPHRASE_FRAGMENT_TAG)
                   ?: PassphraseFragment()) as PassphraseFragment

        // if current item was selected
        if (item.itemId == mNavMenuItemId) return true

        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_password -> {
                addFragment(pwf, PASSWORD_FRAGMENT_TAG)
                mNavMenuItemId = item.itemId
            }
            R.id.nav_passphrase -> {
                addFragment(ppf, PASSPHRASE_FRAGMENT_TAG)
                mNavMenuItemId = item.itemId
            }
            R.id.nav_manage -> startActivity(Intent(this, SettingsActivity::class.java))
            else -> return false
        }
        // do not assign mNavMenuId here so that drawer reverts to right selection when
        // coming back from the SettingsActivity

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun addFragment(fragment: Fragment, tag: String) {
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment, tag)
                .addToBackStack(tag)
                .commit()
    }

    companion object {
        private val PASSWORD_FRAGMENT_TAG = "password_fragment"
        private val PASSPHRASE_FRAGMENT_TAG = "passphrase_fragment"
        private val NAV_MENU_ITEM_TAG = "nav_menu_item"
    }
}
