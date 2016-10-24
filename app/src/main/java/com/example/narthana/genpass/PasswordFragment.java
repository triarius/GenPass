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
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_password, container, false);

        final Button btnGenerate = (Button) rootView.findViewById(R.id.button_generate_password);
        btnGenerate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TextView passText = (TextView) rootView.findViewById(R.id.textview_password);
                int len = numChars();
                passText.setText(Utility.newPassword(len));
            }
        });

        return rootView;
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
