package com.example.narthana.genpass;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by narthana on 28/12/16.
 */

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
    private static final int DEFAULT_VALUE = 0;
    private static final int MAX_VALUE = 100;

    private SeekBar mSeekBar;
    private TextView mValueText;

//    private final AttributeSet mAttrs;

    private int mValue;
    private boolean mValueSet;

    public SeekBarPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
//        mAttrs = attrs;
        mSeekBar = new SeekBar(context, attrs);
    }

    @Override
    protected View onCreateDialogView()
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);

        ViewGroup seekBarParent = (ViewGroup) mSeekBar.getParent();
        if (seekBarParent != null)
        {
            seekBarParent.removeAllViews();
            mValue = getPersistedInt(DEFAULT_VALUE);
            Log.d(getClass().getSimpleName(), "just removed parents");
        }
//        mSeekBar = new SeekBar(getContext(), mAttrs);
        mSeekBar.setOnSeekBarChangeListener(this);

        mValueText = new TextView(getContext(), null);
        mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueText.setTextSize(20);

        layout.addView(mValueText, lp);
        layout.addView(mSeekBar, lp);

        return layout;
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);
        mSeekBar.setProgress(mValue);
        Log.d(getClass().getSimpleName(), "in onBindDialogView");
        mValueText.setText(String.valueOf(mSeekBar.getProgress()));
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
    {
        if (restorePersistedValue) mValue = getPersistedInt(DEFAULT_VALUE);
        else
        {
            mValue = (Integer) defaultValue;
            persistInt(mValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    { return a.getInteger(index, DEFAULT_VALUE); }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        if (positiveResult) persistInt(mValue);
        else
        {
//            mValue = getPersistedInt(DEFAULT_VALUE);
            Log.d(getClass().getSimpleName(), "In onDialogClosed");
        }
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();

        // If persistent, can return superclass's state
        if (isPersistent()) return superState;

        final SavedState state = new SavedState(superState);
        state.value = mValue;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        // Check whether we have not already saved the state
        if (state == null || !state.getClass().equals(SavedState.class))
        {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState castState = (SavedState) state;
        super.onRestoreInstanceState(castState.getSuperState());

        // restore widget state
        mSeekBar.setProgress(castState.value);
    }

    private static class SavedState extends BaseSavedState
    {
        // Member that holds the setting's value
        int value;

        public SavedState(Parcelable superState) { super(superState); }

        public SavedState(Parcel source)
        {
            super(source);

            // Get the current preference's value
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // Write the preference's value
            dest.writeInt(value);
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>()
        {

            public SavedState createFromParcel(Parcel in) { return new SavedState(in); }

            public SavedState[] newArray(int size) { return new SavedState[size]; }
        };
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        mValueText.setText(String.valueOf(progress));
        mValue = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}
}
