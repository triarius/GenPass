package com.example.android.genpass.preference;

import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

/**
 * Created by narthana on 28/06/17.
 */

final class MSMLSavePreference {
    static void save(SharedPreferences.Editor editor, String key, Map<String, Set<String>> values) {
        editor.putBoolean(key, true);
        for (Map.Entry<String, Set<String>> e : values.entrySet())
            editor.putStringSet(key + e.getKey(), e.getValue());
        editor.apply();
    }
}
