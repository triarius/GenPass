package com.example.narthana.genpass.preference;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by narthana on 26/01/17.
 */

class Utility
{
    static int dpToPx(Context context, int length)
    {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                length,
                context.getResources().getDisplayMetrics()
        );
    }
}
