package com.example.narthana.genpass;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Random;

/**
 * Created by narthana on 22/10/16.
 */

public class Utility
{
    private static int numChars(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(
                "numChars",
                context.getResources().getInteger(R.integer.pref_default_password_length)
        );
    }

    static String newPass(Context context)
    {
        Random r = new Random();
        String charSet = "!@#$%^&*()_+1234567890-=qwertyuiop[]asdfghjkl;'zxcvbnm,./QWERTYUIOP{}ASDFGHJKL:|\\\"ZXCVBNM<>?";
        int len = numChars(context);
        char[] pass = new char[len];
        for (int i = 0; i < len; ++i)
            pass[i] = charSet.charAt(r.nextInt(charSet.length()));
        return new String(pass);
    }
}
