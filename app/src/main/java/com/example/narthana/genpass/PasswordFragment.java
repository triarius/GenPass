package com.example.narthana.genpass;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by narthana on 22/10/16.
 */

public class PasswordFragment extends Fragment
{
    private final String PASSWORD_TAG = "password";

    private String mPassText;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) mPassText = savedInstanceState.getString(PASSWORD_TAG);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_password, container, false);
        final TextView passTextView = (TextView) rootView.findViewById(R.id.textview_password);
        final Button btnGenerate = (Button) rootView.findViewById(R.id.button_generate_password);

        if (mPassText != null) passTextView.setText(mPassText);

        btnGenerate.setOnClickListener((View view) -> {
            int len = numChars();
            mPassText = newPassword(len);
            passTextView.setText(mPassText);
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (mPassText != null) outState.putString(PASSWORD_TAG, mPassText);
    }

    private static String newPassword(int len)
    {
        Random r = new Random();
        String charSet = "!@#$%^&*()_+1234567890-=qwertyuiop[]asdfghjkl;'zxcvbnm,./QWERTYUIOP{}ASDFGHJKL:|\\\"ZXCVBNM<>?";
        char[] pass = new char[len];
        for (int i = 0; i < len; ++i)
            pass[i] = charSet.charAt(r.nextInt(charSet.length()));
        return new String(pass);
    }

    private int numChars()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getInt(
                "numChars",
                getActivity().getResources().getInteger(R.integer.pref_default_password_length)
        );
    }
}
