package com.example.narthana.genpass.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.preference.DialogPreference
import android.preference.Preference
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

/**
 * Created by narthana on 28/12/16.
 */

class SeekBarPreference @JvmOverloads
        constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
                    defStyleRes: Int = defStyleAttr):
        DialogPreference(context, attrs), SeekBar.OnSeekBarChangeListener {
    private val mSeekBar: SeekBar = SeekBar(context, attrs)
    private val mValueText: TextView = TextView(context, null)

    private var mValue: Int = DEFAULT_VALUE

    override fun onCreateDialogView(): View {
        // create a linear layout container
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        val pad = Utility.dpToPx(context, LAYOUT_PADDING)
        layout.setPadding(pad, pad, pad, pad)

        // remove seekbar from previous parent (if any)
        (mSeekBar.parent as ViewGroup?)?.removeView(mSeekBar)
        (mValueText.parent as ViewGroup?)?.removeView(mValueText)

        // get the value
        mValue = getPersistedInt(DEFAULT_VALUE)
        // set the change listener
        mSeekBar.setOnSeekBarChangeListener(this)

        mValueText.gravity = Gravity.CENTER_HORIZONTAL
        mValueText.textSize = TEXT_SIZE

        layout.addView(mValueText, lp)
        layout.addView(mSeekBar, lp)

        return layout
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        mSeekBar.progress = mValue
        mValueText.text = mSeekBar.progress.toString()
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue)
            mValue = getPersistedInt(DEFAULT_VALUE)
        else {
            mValue = defaultValue as Int
            persistInt(mValue)
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int) = a.getInt(index, DEFAULT_VALUE)

    override fun onDialogClosed(positiveResult: Boolean) { if (positiveResult) persistInt(mValue) }

    // handle saved states
    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        // commented out to support rotation
        //        // If persistent, can return superclass's state
        //        if (isPersistent()) return superState;

        val state = SavedState(superState)
        state.value = mValue
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        // check whether state is one that we have already saved
        if (state == null || state.javaClass != SavedState::class.java) {
            super.onRestoreInstanceState(state)
            return
        }

        val castState = state as SavedState
        super.onRestoreInstanceState(castState.superState)

        // restore widget state
        mValue = castState.value
        mSeekBar.progress = mValue
    }

    private class SavedState: Preference.BaseSavedState {
        // Member that holds the setting's value
        internal var value: Int = 0

        internal constructor(superState: Parcelable) : super(superState) {}

        internal constructor(source: Parcel) : super(source) { value = source.readInt() }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(value)
        }

        companion object {
            // Standard creator object using an instance of this class
            val CREATOR: Parcelable.Creator<SavedState> = object: Parcelable.Creator<SavedState> {
                override fun createFromParcel(inParcel: Parcel) = SavedState(inParcel)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    // seekbar listener
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        mValueText.text = progress.toString()
        mValue = progress
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    companion object {
        private val DEFAULT_VALUE = 0
        private val LAYOUT_PADDING = 3.0f
        private val TEXT_SIZE = 20.0f
    }
}
