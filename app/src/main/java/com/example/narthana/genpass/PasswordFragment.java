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
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private final int NUM_CHARCLASSES = 4;

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

        final CheckBox cbLowerEn = (CheckBox) rootView.findViewById(R.id.password_lower_enabled);
        final CheckBox cbLowerMan = (CheckBox) rootView.findViewById(R.id.password_lower_mandatory);
        final CheckBox cbUpperEn = (CheckBox) rootView.findViewById(R.id.password_upper_enabled);
        final CheckBox cbUpperMan = (CheckBox) rootView.findViewById(R.id.password_upper_mandatory);
        final CheckBox cbNumEn = (CheckBox) rootView.findViewById(R.id.password_numeric_enabled);
        final CheckBox cbNumMan = (CheckBox) rootView.findViewById(R.id.password_numeric_mandatory);
        final CheckBox cbSymEn = (CheckBox) rootView.findViewById(R.id.password_symbols_enabled);
        final CheckBox cbSymMan = (CheckBox) rootView.findViewById(R.id.password_symbols_mandatory);

        final SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        final CheckBox[] checkBoxes = new CheckBox[]{cbLowerEn, cbUpperEn, cbNumEn, cbSymEn,
                cbLowerMan, cbUpperMan, cbNumMan, cbSymMan};
        final int[] prefIds = new int[] {R.string.pref_password_lower_enabled,
                R.string.pref_password_upper_enabled, R.string.pref_password_numeric_enabled,
                R.string.pref_password_symbol_enabled, R.string.pref_password_lower_mandatory,
                R.string.pref_password_upper_mandatory, R.string.pref_password_numeric_mandatory,
                R.string.pref_password_symbol_mandatory};
        final boolean[] defCBStates = {true, true, true, false, false, false, false, false};

        // Set texts
        if (mPassText != null) tvPass.setText(mPassText);
        setSeekBarText(tvPassLength, sbLength.getProgress());

        // attach click listener to button
        btnGenerate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mPassText = newPassword(numChars(), prefIds, defCBStates);
                tvPass.setText(mPassText);
            }
        });

        // set slider to saved pw length
        int defaultLength = numChars();
        setSeekBarText(tvPassLength, defaultLength);
        sbLength.setProgress(defaultLength);

        // seek bar click listener
        sbLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean user)
            {
                setSeekBarText(tvPassLength, progress);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(getResources().getString(R.string.pref_password_length), progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        // create checkboxes
        for (int i = 0; i < checkBoxes.length; ++i)
        {
            setCheckBoxToPref(checkBoxes[i], prefIds[i], defCBStates[i]);

            final int j = i; // so we can access i from the inner class
            checkBoxes[j].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                {
                    // if the checkbox is enabling, en/disable the corresponding mandating checkbox
                    if (j < NUM_CHARCLASSES)
                    {
                        CheckBox manCB = checkBoxes[j + NUM_CHARCLASSES];
                        manCB.setEnabled(b);
                        if (!b) manCB.setChecked(b);
                    }

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(getResources().getString(prefIds[j]), b);
                    editor.apply();
                }
            });

            // if a mandating checkbox, dis/enable it if corresponding enabling cb is un/checked
            if (i >= NUM_CHARCLASSES)
            {
                int enId = i - NUM_CHARCLASSES;
                boolean enabled = prefs.getBoolean(
                        getResources().getString(prefIds[enId]),
                        defCBStates[enId]
                );
                checkBoxes[i].setEnabled(enabled);
            }
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (mPassText != null) outState.putString(PASSWORD_TAG, mPassText);
    }

    private String newPassword(int len, int[] prefIds, boolean[] defaults)
    {
        Random r = new Random();
        String charSet = "";

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Resources resources = getResources();

        String lower = resources.getString(R.string.lowercase);
        String upper = resources.getString(R.string.uppercase);
        String numeric = resources.getString(R.string.numeric);
        String symbols = resources.getString(R.string.symbols);

        String[] charSets = new String[] {lower, upper, numeric, symbols};

        boolean nonEmptyCharSet = false;

        for (int i = 0; i < charSets.length; ++i)
        {
            if (prefs.getBoolean(resources.getString(prefIds[i]), defaults[i]))
            {
                charSet += charSets[i];
                nonEmptyCharSet = true;
            }
        }

        if (!nonEmptyCharSet) return "";

        char[] pass = new char[len];
        for (int i = 0; i < len; ++i)
            pass[i] = charSet.charAt(r.nextInt(charSet.length()));
        return new String(pass);
    }

    private int numChars()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getInt(
                getResources().getString(R.string.pref_password_length),
                getResources().getInteger(R.integer.pref_default_password_length)
        );
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void setSeekBarText(TextView textView, int progress)
    {
        Locale locale = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ?
            getResources().getConfiguration().getLocales().get(0) :
            getResources().getConfiguration().locale;

        textView.setText(String.format(locale, "%d", progress));
    }

    private void setCheckBoxToPref(CheckBox cb, int prefIds, boolean fallback)
    {
        Resources res = getResources();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        cb.setChecked(prefs.getBoolean(res.getString(prefIds), fallback));
    }
}
