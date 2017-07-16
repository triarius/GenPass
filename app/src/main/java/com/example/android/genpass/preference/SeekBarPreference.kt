package com.example.android.genpass.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.preference.DialogPreference
import android.preference.Preference
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.example.android.genpass.R

/**
 * Created by narthana on 28/12/16.
 */

class SeekBarPreference (context: Context, attrs: AttributeSet):
        DialogPreference(context, attrs), SeekBar.OnSeekBarChangeListener {
    private val mSeekBar: SeekBar = SeekBar(context, null).apply {
        setOnSeekBarChangeListener(this@SeekBarPreference)
    }
    private val mValueText: TextView = TextView(context, null).apply {
        gravity = Gravity.CENTER_HORIZONTAL
        textSize = TEXT_SIZE
    }

    private var mValue: Int = DEFAULT_VALUE // don't get int here, update in onBind, allows cancel

    private val minVal: Int
    private val maxVal: Int

    init {
        val styledAttrs = context.theme
                .obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, 0, 0)
        val minKey = styledAttrs.getString(R.styleable.SeekBarPreference_minPrefKey)
        val maxKey = styledAttrs.getString(R.styleable.SeekBarPreference_maxPrefKey)
        val max = styledAttrs.getInt(R.styleable.SeekBarPreference_android_max, DEFAULT_MAX)
        minVal = PreferenceManager.getDefaultSharedPreferences(context).getInt(minKey, 0)
        maxVal = PreferenceManager.getDefaultSharedPreferences(context).getInt(maxKey, max)
        mSeekBar.max = maxVal - minVal
    }

    override fun onCreateDialogView(): View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        val pad = LAYOUT_PADDING.dpToPx(context)
        setPadding(pad, pad, pad, pad)

        mValue = getPersistedInt(DEFAULT_VALUE)

        // remove seekbar from previous parent (if any)
        (mSeekBar.parent as ViewGroup?)?.removeView(mSeekBar)
        (mValueText.parent as ViewGroup?)?.removeView(mValueText)

        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        addView(mValueText, lp)
        addView(mSeekBar, lp)
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        mValue = getPersistedInt(DEFAULT_VALUE)
        mSeekBar.progress = mValue - minVal
        mValueText.text = mValue.toString()
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) mValue = getPersistedInt(DEFAULT_VALUE)
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
        return SavedState(superState).apply { value = mValue }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        // check whether state is one that we have already saved
        if (state == null || state.javaClass != SavedState::class.java) {
            super.onRestoreInstanceState(state)
            return
        }

        // Cast state to custom BaseSavedState
        val castState = state as SavedState

        // restore widget state
        mValue = castState.value
        mSeekBar.progress = mValue - minVal
        super.onRestoreInstanceState(castState.superState)
    }

    private class SavedState: Preference.BaseSavedState {
        // Member that holds the setting's value
        var value: Int = 0

        constructor(superState: Parcelable) : super(superState)
        constructor(source: Parcel) : super(source) { value = source.readInt() }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(value)
            super.writeToParcel(dest, flags)
        }

        companion object {
            @JvmField val CREATOR = creator(::SavedState)
        }
    }

    // seekbar listener
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        mValue = minVal + progress
        mValueText.text = mValue.toString()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    companion object {
        private const val DEFAULT_VALUE = 0
        private const val DEFAULT_MAX = 100
        private const val LAYOUT_PADDING = 3.0f
        private const val TEXT_SIZE = 20.0f
    }
}
