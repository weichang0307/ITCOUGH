package com.example.itcough

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.itcough.`object`.Global

class RecordSetting : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_setting)

        val seekBarMax = findViewById<SeekBar>(R.id.seekBarMax)
        val seekBarMaxValue = findViewById<TextView>(R.id.seekBarMaxValue)

        val seekBarSpacing = findViewById<SeekBar>(R.id.seekBarSpacing)
        val seekBarSpacingValue = findViewById<TextView>(R.id.seekBarSpacingValue)

        val topBarLayout = findViewById<View>(R.id.topBarLayout)
        val btnLeft = topBarLayout.findViewById<ImageButton>(R.id.btnLeft)
        btnLeft.setOnClickListener(){
            finish()
        }

        seekBarSpacing.progress = Global.coughSpacingLength
        seekBarSpacingValue.text = Global.coughSpacingLength.toString()

        seekBarMax.progress = Global.maxCoughAudioLength
        seekBarMaxValue.text = Global.maxCoughAudioLength.toString()

        seekBarMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarMaxValue.text = progress.toString()
                Global.maxCoughAudioLength = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // 開始拖動時的行為
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        seekBarSpacing.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarSpacingValue.text = progress.toString()
                Global.coughSpacingLength = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // 開始拖動時的行為
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // 停止拖動時的行為
            }
        })
    }

}
