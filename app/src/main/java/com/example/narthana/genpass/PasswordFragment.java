package com.example.narthana.genpass;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;
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
        final TextView tvPass = (TextView) rootView.findViewById(R.id.password_textview);
        final TextView tvPassLength = (TextView) rootView.findViewById(R.id.password_length_textview);
        final Button btnGenerate = (Button) rootView.findViewById(R.id.button_generate_password);
        final SeekBar sbLength = (SeekBar) rootView.findViewById(R.id.password_length_seekbar);

        final String pwlenTag = getActivity().getString(R.string.pref_password_length);
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (mPassText != null) tvPass.setText(mPassText);
        setSeekBarText(tvPassLength, sbLength.getProgress());

        btnGenerate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int len = numChars();
                mPassText = newPassword(len);
                tvPass.setText(mPassText);
            }
        });

        sbLength.setProgress(sharedPreferences.getInt(
                pwlenTag,
                getActivity().getResources().getInteger(R.integer.pref_default_password_length)
        ));
        sbLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean user)
            {
                setSeekBarText(tvPassLength, progress);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(pwlenTag, progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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
        Resources res = getActivity().getResources();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getInt(
                res.getString(R.string.pref_password_length),
                res.getInteger(R.integer.pref_default_password_length)
        );
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void setSeekBarText(TextView textView, int progress)
    {
        Locale locale = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ?
        getActivity().getResources().getConfiguration().getLocales().get(0) :
        getResources().getConfiguration().locale;

        textView.setText(String.format(locale, "%d", progress));
    }
}
