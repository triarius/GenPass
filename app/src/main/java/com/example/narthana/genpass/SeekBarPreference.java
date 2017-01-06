package com.example.narthana.genpass;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by narthana on 28/12/16.
 */

public class SeekBarPreference extends DialogPreference
{
    private static final int DEFAULT_VALUE = 15;
    private static final int MAX_VALUE = 201;

    private final int mMax;

    private int mValue;
    private TextView mSeekBarValue;
    private SeekBar mSeekBar;


    public SeekBarPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setDialogLayoutResource(R.layout.seekbar_peference);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SeekBarPreference,
                0,
                0
        );

        try { mMax = a.getInt(R.styleable.SeekBarPreference_max, MAX_VALUE); }
        finally { a.recycle(); }
    }

    @Override
    protected View onCreateDialogView()
    {
        View rootView = super.onCreateDialogView();

        mSeekBarValue = (TextView) rootView.findViewById(R.id.seek_bar_preference_value);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.seek_bar_preference_seek_bar);

        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mValue);
        mSeekBarValue.setText(String.valueOf(mSeekBar.getProgress()));
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                mSeekBarValue.setText(String.valueOf(progress));
                mValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return rootView;
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
    { if (positiveResult) persistInt(mValue); }

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

    private String getXMLResource(Context context, AttributeSet attrs, String namespace, String attribute)
    {
        int id = attrs.getAttributeResourceValue(namespace, attribute, -1);
        return (id == -1) ? attrs.getAttributeValue(namespace, attribute) :
                            context.getString(id);
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
}
