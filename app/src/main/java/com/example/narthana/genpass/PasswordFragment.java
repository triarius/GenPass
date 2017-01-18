package com.example.narthana.genpass;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by narthana on 22/10/16.
 */

public class PasswordFragment extends Fragment
{
    private static final String PASSWORD_TAG = "password";
    private static final String COPYABLE_TAG = "copyable";

    private final SecureRandom mRandom = new SecureRandom();

    private Map<String, String> mKeyToCharset;
    private Set<String> mDefaultCharsetKeys;
    private Set<String> mDefManCharsetKeys;

    private boolean mPasswordCopyable = false;
    private String mPassText;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        final Resources res = getResources();
        final String[] charsets = res.getStringArray(R.array.charsets);
        final String[] charsetKeys = res.getStringArray(R.array.pref_password_charset_keys);

        mKeyToCharset = new HashMap<>(charsets.length);
        for (int i = 0; i < charsets.length; ++i)
            mKeyToCharset.put(charsetKeys[i], charsets[i]);

        mDefaultCharsetKeys = new HashSet<>(Arrays.asList(
                res.getStringArray(R.array.pref_password_charset_default_enabled)
        ));

        mDefManCharsetKeys = new HashSet<>(Arrays.asList(
                res.getStringArray(R.array.pref_password_charset_default_mandatory)
        ));
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            mPassText = savedInstanceState.getString(PASSWORD_TAG);
            mPasswordCopyable = savedInstanceState.getBoolean(COPYABLE_TAG);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_password, container, false);

        final TextView tvPass = (TextView) rootView.findViewById(R.id.password_textview);
        final Button btnGenerate = (Button) rootView.findViewById(R.id.button_generate_password);

        // Set texts
        if (mPassText != null) tvPass.setText(mPassText);

        // set listener to copy password
        tvPass.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mPasswordCopyable)
                {
                    ClipboardManager clipboard = (ClipboardManager)
                            getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(
                            getString(R.string.clipboard_text),
                            tvPass.getText()
                    );
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(rootView, R.string.copy_msg, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        // attach click listener to button
        btnGenerate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mPasswordCopyable = true;
                mPassText = newPassword(numChars(), rootView);

                if (mPassText != null) tvPass.setText(mPassText);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (mPassText != null) outState.putString(PASSWORD_TAG, mPassText);
        outState.putBoolean(COPYABLE_TAG, mPasswordCopyable);
    }

    private String newPassword(int len, View rootView)
    {
        if (len < 1)
        {
            Snackbar.make(rootView, R.string.zero_length, Snackbar.LENGTH_SHORT).show();
            return "";
        }

        final SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        // create charset to draw from
        final Set<String> selectedCharsetKeys = prefs.getStringSet(
                getString(R.string.pref_password_charset_key) + "0",
                mDefaultCharsetKeys
        );
        // collect the mandatory preferences into an array, and count them
        final Set<String> mandatoryCharsetKeys = prefs.getStringSet(
                getString(R.string.pref_password_charset_key) + "1",
                mDefManCharsetKeys
        );

        // the user has not checked any char subsets to add to the charset
        if (selectedCharsetKeys.size() == 0)
        {
            Snackbar.make(rootView, R.string.empty_charset, Snackbar.LENGTH_SHORT).show();
            return null;
        }

        // TODO: prevent the UI from allowing this to occur
        if (mandatoryCharsetKeys.size() > len)
        {
            Snackbar.make(rootView, R.string.too_many_mandates, Snackbar.LENGTH_SHORT).show();
            return null;
        }

        // select the mandated characters
        char[] password = new char[len];

        int pos = 0;
        for (String s : mandatoryCharsetKeys)
        {
            String charSet = mKeyToCharset.get(s);
            password[pos++] = charSet.charAt(mRandom.nextInt(charSet.length()));
        }

        // build the charset for the non mandatory part
        StringBuilder charSetBldr = new StringBuilder();
        for (String s : selectedCharsetKeys)
           charSetBldr.append(mKeyToCharset.get(s));
        String charSet = charSetBldr.toString();

        // fill out rest of the password with arbitrary chars from the entire set
        for (; pos < len; ++pos) password[pos] = charSet.charAt(mRandom.nextInt(charSet.length()));

        // shuffle the password so that the mandatory characters are in random positions
        Utility.shuffle(password);

        return new String(password);
    }

    private int numChars()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getInt(
                getString(R.string.pref_password_length_key),
                getResources().getInteger(R.integer.pref_default_password_length)
        );
    }
}
