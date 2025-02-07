package com.example.itcough.model

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.util.Log
import com.example.itcough.`object`.Global
import com.example.itcough.YamnetModel
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.app.*
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.itcough.MainActivity
import com.example.itcough.NotificationActionReceiver
import com.example.itcough.R
import com.example.itcough.`object`.Connection
import com.example.itcough.`object`.GoogleService
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ContinuousAudioRecorder: Service() {
    private val SAMPLE_RATE = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
    )
    private val FRAME_SIZE = 31200 //SAMPLE_RATE * 2 * 0.975
    private val MARGIN_SIZE_FRAMES = Global.coughSpacingLength
    private val MAX_COUGH_BUFFER_SIZE_FRAMES = Global.maxCoughAudioLength
    private val MARGIN_SIZE = FRAME_SIZE * MARGIN_SIZE_FRAMES
    private val MAX_COUGH_BUFFER_SIZE = FRAME_SIZE * MAX_COUGH_BUFFER_SIZE_FRAMES
    private val COUGH_THRESHOLD = 0.2
    private val CHANNEL_ID = "RecordServiceChannel"


    private var audioRecord: AudioRecord? = null

    private var coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var yamnetModel: YamnetModel

    private var marginFrameAmount = 0
    private var coughFrameAmount = 0

    val coughBuffer = ByteArrayOutputStream()
    val marginBuffer = ByteArray(MARGIN_SIZE)

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onCreate() {
        super.onCreate()
        yamnetModel = YamnetModel(this)
        createServiceNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createServiceNotification())
        startRecording()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    fun startRecording() {

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()


        // Continuous recording and upload every 1 second
        coroutineScope.launch {
            val buffer = ByteArray(FRAME_SIZE)
            var bytesRead: Int
            Global.isRecording = true
            while (Global.isRecording) {
                bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                Thread {
                    if (bytesRead < FRAME_SIZE){
                        if (coughBuffer.size() > 0){
                            coughBuffer.write(buffer, 0, bytesRead)
                            saveCoughAudio()
                        }
                    }else{
                        val output = yamnetModel.runInference(byteArrayToFloatArray(buffer))
                        val confidence = output.get(42)
                        Log.d("myTag","分類ID: cough, 分數: $confidence")
                        if (confidence > COUGH_THRESHOLD){
                            if (coughBuffer.size() == 0){
                                coughBuffer.write(marginBuffer, 0, marginFrameAmount * FRAME_SIZE)
                                coughFrameAmount += marginFrameAmount
                                if (marginFrameAmount > 1) {
                                    val data = coughBuffer.toByteArray()
                                    // 清除指定範圍數據
                                    data.fill(0, 0, (marginFrameAmount - 1) * FRAME_SIZE)
                                    coughBuffer.reset() // 清空緩衝區
                                    coughBuffer.write(data) // 僅保留最後一個框架
                                }
                            }
                            marginFrameAmount = 0
                            coughBuffer.write(buffer, 0, FRAME_SIZE)
                            coughFrameAmount ++
                            if (coughBuffer.size()>MAX_COUGH_BUFFER_SIZE) {
                                saveCoughAudio()
                            }
                            Global.isDetected = true
                        }else{
                            if (coughBuffer.size() > 0) {
                                if (marginFrameAmount < MARGIN_SIZE_FRAMES) {
                                    if (marginFrameAmount > 0) {
                                        // 提取已寫入的數據
                                        val data = coughBuffer.toByteArray()
                                        val start = (coughFrameAmount - 1) * FRAME_SIZE
                                        // 清除指定範圍數據
                                        data.fill(0, start, start + FRAME_SIZE)
                                        // 重置並重新寫入
                                        coughBuffer.reset()
                                        coughBuffer.write(data)
                                    }
                                    coughBuffer.write(buffer, 0, FRAME_SIZE)
                                    coughFrameAmount++
                                    System.arraycopy(buffer, 0, marginBuffer, marginFrameAmount * FRAME_SIZE, FRAME_SIZE)
                                    marginFrameAmount++
                                }else{
                                    if (marginFrameAmount > 1) {
                                        // 提取已寫入的數據
                                        val data = coughBuffer.toByteArray()
                                        val start = (coughFrameAmount - 1) * FRAME_SIZE
                                        // 清除指定範圍數據
                                        data.fill(0, start, start + FRAME_SIZE)
                                        // 重置並重新寫入
                                        coughBuffer.reset()
                                        coughBuffer.write(data)
                                    }
                                    saveCoughAudio()
                                    System.arraycopy(marginBuffer, FRAME_SIZE, marginBuffer, 0, (MARGIN_SIZE_FRAMES - 1) * FRAME_SIZE)
                                    System.arraycopy(buffer, 0, marginBuffer, (MARGIN_SIZE_FRAMES - 1) * FRAME_SIZE, FRAME_SIZE)
                                    marginFrameAmount = MARGIN_SIZE_FRAMES
                                }
                            }else{
                                System.arraycopy(marginBuffer, FRAME_SIZE, marginBuffer, 0, (MARGIN_SIZE_FRAMES - 1) * FRAME_SIZE)
                                System.arraycopy(buffer, 0, marginBuffer, (MARGIN_SIZE_FRAMES - 1) * FRAME_SIZE, FRAME_SIZE)
                                marginFrameAmount = MARGIN_SIZE_FRAMES
                            }
                        }


                    }
                }.start()
            }

        }
    }
    private fun sendCreateCoughAudioRequest(dataArray: ByteArray) {
        val jsonData = Gson().toJson(mapOf(
            "userId" to GoogleService.userID,
            "fileName" to getCurrentFormattedTime()
        ))
        Connection.sendAudioWithJsonPostRequest(
            Connection.SAVE_COUGH_AUDIO_PATH,
            jsonData,
            dataArray,
            onRequestSuccess = {
                sendNotification(this, "File Saved", "")
            }
        )
    }
    private fun createServiceNotification(): Notification {
        // 引用自訂佈局
        val notificationLayout = RemoteViews(packageName, R.layout.notification_detecting)

        // 綁定通知內容
        notificationLayout.setTextViewText(R.id.notification_title, "Cough Detector")
        notificationLayout.setTextViewText(R.id.notification_text, "detecting...")

        // 設置按鈕點擊事件
        val stopIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "ACTION_STOP_RECORDING"
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notificationLayout.setOnClickPendingIntent(R.id.btn_stop, stopPendingIntent)

        // 建立通知
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mic)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // 自訂樣式
            .setCustomContentView(notificationLayout) // 設置自訂佈局
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setDeleteIntent(stopPendingIntent)
            .build()
    }
    private fun createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Custom Recording Service", NotificationManager.IMPORTANCE_HIGH
            ).apply {
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    fun sendNotification(context: Context, title: String, message: String) {
        val CHANNEL_ID = "my_notification_channel"
        val notificationId = 2

        // 創建通知頻道（適用於 Android 8.0 及以上）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "My Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "This is my notification channel"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // 設置點擊通知時的動作
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 創建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_audio_file) // 設定通知圖標
            .setContentTitle(title) // 設定標題
            .setContentText(message) // 設定內容
            .setContentIntent(pendingIntent) // 點擊通知後執行的 Intent
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 設置高優先級（適用於 Android 7.1 以下）
            .build()

        // 發送通知
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
    }
    private fun getCurrentFormattedTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
    private fun saveCoughAudio() {
        val dataArray = coughBuffer.toByteArray()
        coughBuffer.reset()
        coughFrameAmount = 0
        sendCreateCoughAudioRequest(dataArray)
    }
    private fun stopRecording() {
        Global.isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        yamnetModel.close()
    }


    private fun byteArrayToFloatArray(buffer: ByteArray): FloatArray {
        val floatArray = FloatArray(buffer.size / 2) // 每兩個字節組成一個浮點數
        for (i in floatArray.indices) {
            val low = buffer[i * 2].toInt() and 0xFF
            val high = buffer[i * 2 + 1].toInt() shl 8
            val sample = high or low // 合成 16 位有符號整數
            floatArray[i] = sample / 32768.0f // 轉換為範圍 [-1.0, 1.0]
        }
        return floatArray
    }
}
