/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.customview

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.SeekBar
import com.newshunt.appview.R
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.news.util.NewsConstants
import com.newshunt.pref.NewsPreference

class FontSizeChangeView2(context: Context) : Dialog(context) {
    private var progress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.news_item_font)

        progress = PreferenceManager.getPreference(
                NewsPreference.USER_PREF_FONT_PROGRESS,
                NewsConstants.DEFAULT_PROGRESS_COUNT)

        val fontChangeSlider = findViewById<SeekBar>(R.id.fond_size_slider) as SeekBar
        fontChangeSlider.progress = progress
        fontChangeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                progress = seekBar.progress
            }
        })

        val okFontSet = findViewById<View>(R.id.button_ok) as NHTextView
        okFontSet.setOnClickListener {
            PreferenceManager.savePreference(NewsPreference.USER_PREF_FONT_PROGRESS, progress)
            dismiss()
        }

    }
}
