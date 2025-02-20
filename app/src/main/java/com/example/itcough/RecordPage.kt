package com.example.itcough

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import android.Manifest
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.example.itcough.model.ContinuousAudioRecorder
import com.example.itcough.`object`.Connection
import com.example.itcough.`object`.Global
import com.example.itcough.`object`.GoogleService
import com.google.gson.Gson
import kotlin.system.measureTimeMillis

class RecordPage : ComponentActivity() {

    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var btnStop: MaterialButton
    private lateinit var btnMin: MaterialButton
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheet: LinearLayout
    private lateinit var bottomSheetBG:View
    private lateinit var tvTimer: ImageButton
    private lateinit var imgAnimation1: ImageView
    private lateinit var imgAnimation2: ImageView
    private lateinit var imgAnimation3: ImageView
    private lateinit var imgAnimation4: ImageView
    private lateinit var tvStatus: TextView



    private var handlerAnimation = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_page)


        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)
        btnRight.isVisible = !Global.isRecording
        btnLeft.setOnClickListener {
            finish()
        }
        btnRight.setBackgroundResource(R.drawable.ic_settings_gray)
        btnRight.setOnClickListener {
            startActivity(Intent(this, RecordSetting::class.java))
        }

        btnStop = findViewById(R.id.btnStop)
        btnMin = findViewById(R.id.btnMin)
        btnStop.setOnClickListener{
            stopRecorder()
        }

        btnMin.setOnClickListener{
            moveTaskToBack(true)
        }

        bottomSheet = findViewById(R.id.bottomSheet)
        bottomSheetBG = findViewById(R.id.bottomSheetBG)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        tvTimer = findViewById(R.id.tvTimer)
        imgAnimation1 = findViewById(R.id.imgAnimation1)
        imgAnimation2 = findViewById(R.id.imgAnimation2)
        imgAnimation3 = findViewById(R.id.imgAnimation3)
        imgAnimation4 = findViewById(R.id.imgAnimation4)
        tvStatus = findViewById(R.id.tvStatus)

        tvTimer.setOnClickListener{
            if(!Global.isRecording){
                showIsPublishDialog()
            }
        }
        if(Global.isRecording){
            tvStatus.text = "Detecting"
            startPulse()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        Global.isDetected = false
    }
    private fun showIsPublishDialog() {
        AlertDialog.Builder(this)
            .setMessage("Are you willing to publish detected coughs to co-create database?")
            .setPositiveButton("Yes") { _, _ ->
                Global.isPublish = true
                showAcknowledgeDialog()
            }
            .setNegativeButton("No") { _, _ ->
                Global.isPublish = false
                showAcknowledgeDialog()
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss() // 只关闭对话框，什么也不做
            }
            .show()
    }
    private fun showAcknowledgeDialog () {
        AlertDialog.Builder(this)
            .setMessage("The cough detector will keep recording in the background\n\n Any other applications using microphone might temporarily block the detector")
            .setPositiveButton("Confirm") { _, _ ->
                startRecording() // 用户确认放弃时调用
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss() // 只关闭对话框，什么也不做
            }
            .show()
    }
    private fun stopRecorder(){
        Log.d("myTag", "stopRecorder")
        Global.isRecording = false
        stopRecording()
        stopRecordUI()
    }
    private fun stopRecordUI() {
        Log.d("myTag", "stopRecorder")
        stopRecording()
        stopPulse()
        dismiss()
        tvStatus.setText("Start")
        tvTimer.isClickable = true
        btnRight.isVisible = true
    }
    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted: Start recording
            startRecording()
        } else {
            // Permission denied: Notify the user
            Toast.makeText(this, "Audio recording permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted: Start recording
            requestAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)

        } else {
            // Permission denied: Notify the user
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    private fun sendStartRecordPostRequest() {
        val jsonData = Gson().toJson(mapOf(
            "userId" to GoogleService.userID,
            "isPublish" to Global.isPublish
        ))
        Log.d("myTag", jsonData.toString())
        Connection.sendJsonPostRequest(
            Connection.START_RECORD_PATH,
            jsonData,
            onRequestSuccess = {
                runOnUiThread{
                    startStreaming()
                }
            },
            onRequestFail = {
                runOnUiThread{
                    Toast.makeText(this, "Fail to start", Toast.LENGTH_SHORT).show()
                    stopRecordUI()
                }
            },
            onConnectionFail = {
                runOnUiThread{
                    Toast.makeText(this, "Fail to connect to server", Toast.LENGTH_SHORT).show()
                    stopRecordUI()
                }
            }
        )
    }

    private fun startRecording(){
        Log.d("myTag", "startRecording")
        val setStr = "Initializing"
        tvStatus.setText(setStr)
        btnRight.isVisible = false
        tvTimer.isClickable = false
        val fadeInAnimator = ObjectAnimator.ofFloat(tvStatus, "alpha", 0f, 1f).apply {
            duration = 1500
            interpolator = AccelerateDecelerateInterpolator()
        }
        fadeInAnimator.start()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Launch the permission request
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        sendStartRecordPostRequest()
    }
    private fun startPulse() {
        cough_detecting.run()
    }
    private fun stopPulse() {
        handlerAnimation.removeCallbacks(cough_detecting)
    }
    private fun dismiss(){
        bottomSheetBG.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
    private fun startStreaming(){
        val intent = Intent(this, ContinuousAudioRecorder::class.java)
        startService(intent)
        Global.isRecording = true
        Global.detectStartTime = System.currentTimeMillis()
        tvStatus.text = "Detecting"
        startPulse()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

    }
    private var cough_detecting = object : Runnable {
        override fun run() {


            if (Global.isDetected) {
                imgAnimation3.animate().scaleX(7f).scaleY(7f).alpha(0f).setDuration(1200)
                    .withEndAction {
                        imgAnimation3.scaleX = 1f
                        imgAnimation3.scaleY = 1f
                        imgAnimation3.alpha = 1f
                    }

                imgAnimation4.animate().scaleX(7f).scaleY(7f).alpha(0f).setDuration(700)
                    .withEndAction {
                        imgAnimation4.scaleX = 1f
                        imgAnimation4.scaleY = 1f
                        imgAnimation4.alpha = 1f
                    }
                Global.isDetected = false
            }else{
                imgAnimation1.animate().scaleX(7f).scaleY(7f).alpha(0f).setDuration(1200)
                    .withEndAction {
                        imgAnimation1.scaleX = 1f
                        imgAnimation1.scaleY = 1f
                        imgAnimation1.alpha = 1f
                    }

                imgAnimation2.animate().scaleX(7f).scaleY(7f).alpha(0f).setDuration(700)
                    .withEndAction {
                        imgAnimation2.scaleX = 1f
                        imgAnimation2.scaleY = 1f
                        imgAnimation2.alpha = 1f
                    }

            }
            if (Global.isRecording) {
                handlerAnimation.postDelayed(this, 1300)
            }else{
                runOnUiThread {
                    Log.d("myTag", "stopRecorder")
                    stopRecording()
                    stopPulse()
                    dismiss()
                    tvStatus.setText("Start")
                }
            }
        }
    }

    private fun stopRecording() {
        val intent = Intent(this, ContinuousAudioRecorder::class.java)
        stopService(intent)
    }
}

