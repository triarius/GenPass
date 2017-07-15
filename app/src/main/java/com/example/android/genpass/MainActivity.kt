package com.example.android.genpass

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

    internal lateinit var charsetMap: Map<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // pick layout
        setSupportActionBar(toolbar) // create action bar

        // create nav Drawer
        val toggle = ActionBarDrawerToggle(
                this,
                drawer_layout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this) // set listener to open drawer

        // get the menu item id or if null, create a new fragment
        mNavMenuItemId = savedInstanceState?.getInt(NAV_MENU_ITEM_TAG) ?: run {
            replaceWith(PasswordFragment()) // open password fragment
            R.id.nav_password // save menu item id for later use in the nav bar
        }

        // set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false)

        // create charset map
        charsetMap = with (resources) {
            getStringArray(R.array.pref_password_charset_keys)
                    .zip(getStringArray(R.array.charsets))
                    .toMap()
        }
    }

    override fun onResume() {
        super.onResume()
        nav_view.setCheckedItem(mNavMenuItemId) // set the selected item in the nav bar
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

    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> consume { startActivity<SettingsActivity>() }
        else -> super.onOptionsItemSelected(item)
    }

    /**
     * If the same item was seletect, do nothing, otherwise, replace with the appropriate task
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean
            = item.itemId == mNavMenuItemId || with (drawer_layout) {
        when (item.itemId) {
            R.id.nav_password -> consume {
                replaceWith(findFragment<PasswordFragment>())
                mNavMenuItemId = item.itemId
            }
            R.id.nav_passphrase -> consume {
                replaceWith(findFragment<PassphraseFragment>())
                mNavMenuItemId = item.itemId
            }
            R.id.nav_manage -> consume { startActivity<SettingsActivity>() }
            else -> false
        }
    }

    companion object {
        private const val NAV_MENU_ITEM_TAG = "nav_menu_item"
    }
}