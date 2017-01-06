package com.example.narthana.genpass;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private static final String PASSWORD_FRAGMENT_TAG = "password_fragment";
    private static final String PASSPHRASE_FRAGMENT_TAG = "passphrase_fragment";
    private static final String NAV_MENU_ITEM_TAG = "nav_menu_item";

    private int mNavMenuItemId;
    private NavigationView mNavView;
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // pick layout
        setContentView(R.layout.activity_main);

        // create action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        // create FAB
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        // create nav Drawer
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        // set listener to open drawer
        mNavView = (NavigationView) findViewById(R.id.nav_view);
        mNavView.setNavigationItemSelectedListener(this);

        // if a new run or if restoring from prev state
        if (savedInstanceState == null)
        {
            // open password fragment
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new PasswordFragment(), PASSWORD_FRAGMENT_TAG)
                    .addToBackStack(PASSWORD_FRAGMENT_TAG)
                    .commit();

            // assign menu item id for later use in the nav bar
            mNavMenuItemId = R.id.nav_password;
        }
        else mNavMenuItemId = savedInstanceState.getInt(NAV_MENU_ITEM_TAG);

        // set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref_password, false);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // set the selected item in the nav bar
        mNavView.setCheckedItem(mNavMenuItemId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_MENU_ITEM_TAG, mNavMenuItemId);
    }

    @Override
    public void onBackPressed()
    {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) mDrawer.closeDrawer(GravityCompat.START);
        else finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        // Gather fragments
        FragmentManager fm = getFragmentManager();
        PasswordFragment pwf = (PasswordFragment) fm.findFragmentByTag(PASSWORD_FRAGMENT_TAG);
        if (pwf == null) pwf = new PasswordFragment();
        PassphraseFragment ppf = (PassphraseFragment) fm.findFragmentByTag(PASSPHRASE_FRAGMENT_TAG);
        if (ppf == null) ppf = new PassphraseFragment();

        int itemId = item.getItemId();

        // if current item was selected
        if (itemId == mNavMenuItemId) return true;

        // Handle navigation view item clicks here.
        switch (itemId)
        {
            case R.id.nav_password:
                addFragment(pwf, PASSWORD_FRAGMENT_TAG);
                mNavMenuItemId = itemId;
                break;
            case R.id.nav_passphrase:
                addFragment(ppf, PASSPHRASE_FRAGMENT_TAG);
                mNavMenuItemId = itemId;
                break;
            case R.id.nav_manage:
                startActivity(new Intent(this, SettingsActivity.class));
                // do not assign mNavMenuId here so that drawer reverts to right selection when
                // coming back from the SettingsActivity
                break;
            default:
                return false;
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void addFragment(Fragment fragment, String tag)
    {
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }
}
