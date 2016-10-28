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

/**
 * Created by narthana on 22/10/16.
 */

public class PasswordFragment extends Fragment
{
    private final String PASSWORD_TAG = "password";

    private String mPassText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_password, container, false);
        final TextView passTextView = (TextView) rootView.findViewById(R.id.textview_password);
        final Button btnGenerate = (Button) rootView.findViewById(R.id.button_generate_password);

        if (savedInstanceState != null)
        {
            mPassText = savedInstanceState.getString(PASSWORD_TAG);
            if (mPassText != null) passTextView.setText(mPassText);
        }

        btnGenerate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int len = numChars();
                mPassText = Utility.newPassword(len);
                passTextView.setText(mPassText);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (mPassText != null) outState.putString(PASSWORD_TAG, mPassText);
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
