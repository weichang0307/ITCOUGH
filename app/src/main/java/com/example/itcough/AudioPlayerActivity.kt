package com.example.itcough

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.itcough.`object`.Global

class AudioPlayerActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying_ = false
    private lateinit var seekBar: SeekBar
    private lateinit var playPauseButton: Button
    private lateinit var tvPath: TextView
    private lateinit var tvName: TextView
    private lateinit var tvTime: TextView
    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var tvTitle: TextView
    private val handler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        val topBarLayout = findViewById<View>(R.id.topBarLayout)
        btnLeft = topBarLayout.findViewById(R.id.btnLeft)
        tvTitle = topBarLayout.findViewById(R.id.tvTitle)
        btnRight = topBarLayout.findViewById(R.id.btnRight)

        btnLeft.setOnClickListener {
            finish()
        }

        btnRight.setOnClickListener {
            finish()
        }

        val path = intent.getStringExtra("FILE_PATH") ?: ""
        Log.d("myTest", path)

        val name = intent.getStringExtra("FILE_NAME") ?: ""
        tvPath = findViewById(R.id.tvPath)
        tvPath.setText(path)
        tvName = findViewById(R.id.tvName)
        tvName.setText(name)
        tvTime = findViewById(R.id.tvTime)
        tvTime.setText("00:00")
        if (path.isEmpty()) {
            Toast.makeText(this, "No audio URL provided", Toast.LENGTH_SHORT).show()
            return
        }
        val url = "${Global.URL}/" + "get_uploads_file/" + path.replace("\\", "^")
        Log.d("myTag", url)
        // 播放/暫停按鈕
        playPauseButton = findViewById(R.id.playPauseButton)
        playPauseButton.alpha = 0.1f
        prepareAudio(url)

        // SeekBar
        seekBar = findViewById(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.let{
                        it.seekTo(progress * it.duration / seekBar!!.max)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun prepareAudio(url: String) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener {
                    playPauseButton.setOnClickListener {
                        if (isPlaying_) {
                            pauseAudio()
                        } else {
                            playAudio()
                        }
                    }
                    playPauseButton.alpha = 1f
                    playAudio()
                    startUpdatingSeekBar()
                }
                setOnCompletionListener {
                    pauseAudio()
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@AudioPlayerActivity, "Error playing audio", Toast.LENGTH_SHORT).show()
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun playAudio() {
        mediaPlayer?.start()
        isPlaying_ = true
        playPauseButton.text = "Pause"
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()

        isPlaying_ = false
        playPauseButton.text = "Play"
    }

    private fun startUpdatingSeekBar() {
        val runnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    runOnUiThread {
                        val currentPosition = it.currentPosition
                        seekBar.progress = seekBar.max * currentPosition / it.duration
                        val positionInSeconds = currentPosition/1000
                        tvTime.setText("${String.format("%02d", positionInSeconds/60)}:${String.format("%02d", positionInSeconds%60)}")
                    }
                    handler.postDelayed(this, 100)
                }
            }
        }
        runnable.run()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        handler.removeCallbacksAndMessages(null)  // 清除 Handler
    }
}
