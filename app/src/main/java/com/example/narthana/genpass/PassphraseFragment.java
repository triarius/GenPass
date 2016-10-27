package com.example.narthana.genpass;

import android.app.Fragment;
import android.os.Bundle;
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

public class PassphraseFragment extends Fragment
{
    private String[] mWords;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_passphrase, container, false);

        final int n = 4;
        final int maxWordLength = 10;
        final int minWordLength = 5;
        final String delim = " ";
//        InputStream dictionary = rootView.getResources().openRawResource(R.raw.default_dictionary);
//        Utility.loadDictionary(rootView.getContext(), dictionary);

        mWords = Utility.getWords(rootView.getContext(), minWordLength, maxWordLength);

        final Random r = new Random();
        final Button btnGenerate = (Button) rootView.findViewById(R.id.button_generate_passphrase);

        btnGenerate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                for (int i = 0; i < n; ++i)
                {
                    int j = r.nextInt(mWords.length - i) + i; // random int in [i, words.length)
                    swap(mWords, i, j);
                }
                String passphrase = concatenateRange(mWords, delim, 0, n - 1);

                TextView passText = (TextView) rootView.findViewById(R.id.textview_passphrase);
                passText.setText(passphrase);
            }
        });

        return rootView;
    }

    private <T> void swap(T[] array, int i, int j)
    {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    private String concatenateRange(String[] words, String delim, int start, int end)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; ++i)
        {
            builder.append(words[i]);
            builder.append(delim);
        }
        builder.append(words[end]);
        return builder.toString();
    }
}
