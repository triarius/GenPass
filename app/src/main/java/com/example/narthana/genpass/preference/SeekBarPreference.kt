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

class SeekBarPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = defStyleAttr) : DialogPreference(context, attrs), SeekBar.OnSeekBarChangeListener {

    private val mSeekBar: SeekBar
    private var mValueText: TextView? = null

    private var mValue: Int = 0

    init { mSeekBar = SeekBar(context, attrs) } // we need to create this here to pass attrs ???

    override fun onCreateDialogView(): View {
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // create a linear layout container
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        val pad = Utility.dpToPx(context, 3)
        layout.setPadding(pad, pad, pad, pad)

        // remove seekbar from previous parent
        val seekBarParent = mSeekBar.parent as ViewGroup?
        seekBarParent?.removeAllViews()
        mValue = getPersistedInt(DEFAULT_VALUE)
        mSeekBar.setOnSeekBarChangeListener(this)

        mValueText = TextView(context, null)
        mValueText!!.gravity = Gravity.CENTER_HORIZONTAL
        mValueText!!.textSize = 20f

        layout.addView(mValueText, lp)
        layout.addView(mSeekBar, lp)

        return layout
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        mSeekBar.progress = mValue
        mValueText!!.text = mSeekBar.progress.toString()
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue)
            mValue = getPersistedInt(DEFAULT_VALUE)
        else {
            mValue = defaultValue as Int
            persistInt(mValue)
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, DEFAULT_VALUE)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) persistInt(mValue)
    }

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
        // check whether we have already saved the state
        if (state == null || state.javaClass != SavedState::class.java) {
            super.onRestoreInstanceState(state)
            return
        }

        val castState = state as SavedState?
        super.onRestoreInstanceState(castState!!.getSuperState())

        // restore widget state
        mValue = castState.value
        mSeekBar.progress = mValue
    }

    private class SavedState : Preference.BaseSavedState {
        // Member that holds the setting's value
        internal var value: Int = 0

        internal constructor(superState: Parcelable) : super(superState) {}

        internal constructor(source: Parcel) : super(source) {

            // Get the current preference's value
            value = source.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)

            // Write the preference's value
            dest.writeInt(value)
        }

        companion object {

            // Standard creator object using an instance of this class
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    // seekbar listener
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        mValueText!!.text = progress.toString()
        mValue = progress
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    companion object {
        private val DEFAULT_VALUE = 0
    }
}
