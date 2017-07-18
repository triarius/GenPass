package com.example.android.genpass.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.preference.DialogPreference
import android.preference.Preference
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.example.android.genpass.R

/**
 * Created by narthana on 28/12/16.
 */

class SeekBarPreference (context: Context, attrs: AttributeSet):
        DialogPreference(context, attrs), SeekBar.OnSeekBarChangeListener {
    private val seekBar: SeekBar = SeekBar(context, null).apply {
        setOnSeekBarChangeListener(this@SeekBarPreference)
    }
    private val valueText: TextView = TextView(context, null).apply {
        gravity = Gravity.CENTER_HORIZONTAL
        textSize = TEXT_SIZE
    }

    private var value: Int = DEFAULT_VALUE // don't get int here, update in onBind, allows cancel
    private var max = DEFAULT_MAX

    // if the min/maxPrefKey attributes are set, derive the min/maxVal from the prefs dynamically
    // otherwise use 0 and the android:max attribute value
    private var minKey: String? = null
    private var maxKey: String? = null

    private val minVal: Int
        get() = keyToPref(minKey, 0)
    private val maxVal: Int
        get() = keyToPref(maxKey, max)

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, 0, 0).use {
            if (hasValue(R.styleable.SeekBarPreference_minPrefKey))
                minKey = getString(R.styleable.SeekBarPreference_minPrefKey)
            if (hasValue(R.styleable.SeekBarPreference_maxPrefKey))
                maxKey = getString(R.styleable.SeekBarPreference_maxPrefKey)
            if (hasValue(R.styleable.SeekBarPreference_android_max))
                max = getInt(R.styleable.SeekBarPreference_android_max, DEFAULT_MAX)
        }
    }

    private fun keyToPref(key: String?, defaultValue: Int) = key?.run {
        sharedPreferences.getInt(this, defaultValue)
    } ?: defaultValue

    override fun onCreateDialogView(): View {
        seekBar.max = maxVal - minVal
        value = getPersistedInt(DEFAULT_VALUE)

        // remove seekbar from previous parent (if any)
        seekBar.removeFromParent()
        valueText.removeFromParent()

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = LAYOUT_PADDING.dpToPx(context)
            setPadding(pad, pad, pad, pad)

            val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addView(valueText, lp)
            addView(seekBar, lp)
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        value = getPersistedInt(DEFAULT_VALUE)
        seekBar.progress = value - minVal
        valueText.text = value.toString()
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) value = getPersistedInt(DEFAULT_VALUE)
        else {
            value = defaultValue as Int
            persistInt(value)
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int) = a.getInt(index, DEFAULT_VALUE)

    override fun onDialogClosed(positiveResult: Boolean) { if (positiveResult) persistInt(value) }

    // handle saved states
    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply { value = this@SeekBarPreference.value }
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
        value = castState.value
        seekBar.progress = value - minVal
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

        companion object @JvmField val CREATOR = creator(::SavedState)
    }

    // seekbar listener
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        value = minVal + progress
        valueText.text = value.toString()
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