package com.example.narthana.genpass

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mNavMenuItemId: Int = 0
    private var mNavView: NavigationView? = null
    private var mDrawer: DrawerLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // pick layout
        setContentView(R.layout.activity_main)

        // create action bar
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // create nav Drawer
        mDrawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mDrawer!!.addDrawerListener(toggle)
        toggle.syncState()

        // set listener to open drawer
        mNavView = findViewById(R.id.nav_view) as NavigationView
        mNavView!!.setNavigationItemSelectedListener(this)

        // if a new run or if restoring from prev state
        if (savedInstanceState == null) {
            // open password fragment
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, PasswordFragment(), PASSWORD_FRAGMENT_TAG)
                    .addToBackStack(PASSWORD_FRAGMENT_TAG)
                    .commit()

            // assign menu item id for later use in the nav bar
            mNavMenuItemId = R.id.nav_password
        } else mNavMenuItemId = savedInstanceState.getInt(NAV_MENU_ITEM_TAG)

        // set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false)
    }

    override fun onResume() {
        super.onResume()
        // set the selected item in the nav bar
        mNavView!!.setCheckedItem(mNavMenuItemId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(NAV_MENU_ITEM_TAG, mNavMenuItemId)
    }

    override fun onBackPressed() {
        if (mDrawer!!.isDrawerOpen(GravityCompat.START))
            mDrawer!!.closeDrawer(GravityCompat.START)
        else
            finish()
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
        val fm = fragmentManager
        val pwf = (fm.findFragmentByTag(PASSWORD_FRAGMENT_TAG) ?: PasswordFragment()) as PasswordFragment
        val ppf = (fm.findFragmentByTag(PASSPHRASE_FRAGMENT_TAG) ?: PassphraseFragment()) as PassphraseFragment

        val itemId = item.itemId

        // if current item was selected
        if (itemId == mNavMenuItemId) return true

        // Handle navigation view item clicks here.
        when (itemId) {
            R.id.nav_password -> {
                addFragment(pwf, PASSWORD_FRAGMENT_TAG)
                mNavMenuItemId = itemId
            }
            R.id.nav_passphrase -> {
                addFragment(ppf, PASSPHRASE_FRAGMENT_TAG)
                mNavMenuItemId = itemId
            }
            R.id.nav_manage -> startActivity(Intent(this, SettingsActivity::class.java))
            else -> return false
        }// do not assign mNavMenuId here so that drawer reverts to right selection when
        // coming back from the SettingsActivity

        mDrawer!!.closeDrawer(GravityCompat.START)
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
