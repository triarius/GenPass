package com.example.narthana.genpass;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private final String PASSWORD_FRAGMENT_TAG = "password_fragment";
    private final String PASSPHRASE_FRAGMENT_TAG = "passphrase_fragment";

    private DrawerLayout mDrawer;
    private PasswordFragment mPasswordFragment;
    private PassphraseFragment mPassphraseFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener((View view) ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        );

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            mPasswordFragment = (PasswordFragment) getFragmentManager()
                    .getFragment(savedInstanceState, PASSWORD_FRAGMENT_TAG);
            mPassphraseFragment = (PassphraseFragment) getFragmentManager()
                    .getFragment(savedInstanceState, PASSPHRASE_FRAGMENT_TAG);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (mPasswordFragment != null && mPasswordFragment.isAdded())
            getFragmentManager().putFragment(outState, PASSWORD_FRAGMENT_TAG, mPasswordFragment);
        if (mPassphraseFragment != null && mPassphraseFragment.isAdded())
            getFragmentManager().putFragment(outState, PASSPHRASE_FRAGMENT_TAG, mPassphraseFragment);
    }

    @Override
    public void onBackPressed()
    {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) mDrawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment;
        if (mPasswordFragment == null) mPasswordFragment = new PasswordFragment();
        if (mPassphraseFragment == null) mPassphraseFragment = new PassphraseFragment();

        switch (id)
        {
            case R.id.nav_password:
                fragment = mPasswordFragment;
                break;
            case R.id.nav_passphrase:
                fragment = mPassphraseFragment;
                break;
            default:
                return false;
        }

        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
