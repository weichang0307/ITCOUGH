package com.example.itcough

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.example.itcough.`object`.AudioProcessor
import com.example.itcough.`object`.Connection
import com.example.itcough.`object`.DialogManager
import com.example.itcough.`object`.GoogleService
import com.google.gson.Gson

class ImportActivity : ComponentActivity() {
    private lateinit var tvFilePath: TextView
    private lateinit var btnUploadCough: Button
    private lateinit var btnUploadMusic: Button
    private lateinit var btnChooseFile: Button
    private lateinit var btnPlay: Button
    private lateinit var processingMask: LinearLayout
    private var audioUri: Uri? = null
    private var audioName: String? = null
    private var isChoose = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)
        tvFilePath = findViewById(R.id.tvFilePath)
        btnChooseFile = findViewById(R.id.btnChooseFile)
        btnUploadCough = findViewById(R.id.btnUploadCough)
        btnUploadMusic = findViewById(R.id.btnUploadMusic)
        btnPlay = findViewById(R.id.btnPlay)
        processingMask = findViewById(R.id.processingMask)
        val topBarLayout = findViewById<View>(R.id.topBarLayout)
        val btnLeft = topBarLayout.findViewById<ImageButton>(R.id.btnLeft)
        val btnRight = topBarLayout.findViewById<ImageButton>(R.id.btnRight)
        btnRight.isVisible = false
        btnLeft.setOnClickListener {
            finish()
        }
        btnChooseFile.setOnClickListener {
            openAudioFilePicker()
        }
        btnUploadCough.setOnClickListener {
            if (isChoose) {
                showSaveCoughDialog()
            }
        }
        btnUploadMusic.setOnClickListener {
            if (isChoose) {
                showSaveMusicDialog()
            }
        }
        btnPlay.setOnClickListener {
            if (isChoose) {
                val intent = Intent(this, AudioPlayerActivity::class.java)
                intent.putExtra("FILE_URI", audioUri.toString()) // 假設 record.filePath 是正確的音頻文件路徑
                intent.putExtra("FILE_NAME", audioName) // 假設 record.filePath 是正確的音頻文件路徑
                startActivity(intent)
            }
        }
    }
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            audioUri = result.data!!.data
            if (audioUri != null) {
                isChoose = true
                btnPlay.isVisible = true
                btnUploadMusic.isVisible = true
                btnUploadCough.isVisible = true
                audioName = getFileName(this, audioUri!!)
                tvFilePath.text = audioName
            } else {
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun openAudioFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*" // Only show audio files (MP3, WAV, etc.)
        }
        filePickerLauncher.launch(intent)
    }

    // Handle the selected file


    // Function to get the file name
    private fun getFileName(context: Context, uri: Uri): String {
        var fileName = "Unknown Audio"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex != -1) {
                    fileName = it.getString(columnIndex)
                }
            }
        }
        return fileName
    }
    private fun showSaveCoughDialog() {
        DialogManager.showActionWithInputTextDialog(
            this,
            "Upload As Cough ",
            "upload",
            onSave = { _, text ->
                processingMask.isVisible = true
                if (text.isNotEmpty() && !text.contains('^')) {
                    val pcmArray = AudioProcessor.convertToPCM(this, audioUri!!)
                    if (pcmArray != null) {
                        sendCreateCoughAudioRequest(pcmArray, text)
                    }else {
                        processingMask.isVisible = false
                        Toast.makeText(this, "Fail to convert Audio File for Uploading", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    processingMask.isVisible = false
                    if (text.isEmpty()) {
                        Toast.makeText(
                            this,
                            "file name can not be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "file name can not contain \"^\"",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        )
    }
    private fun showSaveMusicDialog() {
        DialogManager.showActionWithInputTextDialog(
            this,
            "Upload As Music",
            "upload",
            onSave = { _, text ->
                processingMask.isVisible = true
                if (text.isNotEmpty() && !text.contains('^')) {
                    val pcmArray = AudioProcessor.convertToPCM(this, audioUri!!)
                    if (pcmArray != null) {
                        sendCreateMusicAudioRequest(pcmArray, text)
                    }else {
                        processingMask.isVisible = false
                        Toast.makeText(this, "Fail to convert Audio File for Uploading", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    processingMask.isVisible = false
                    if (text.isEmpty()) {
                        Toast.makeText(
                            this,
                            "file name can not be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "file name can not contain \"^\"",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        )
    }





    private fun sendCreateCoughAudioRequest(dataArray: ByteArray, fileName: String) {
        val jsonData = Gson().toJson(mapOf(
            "userId" to GoogleService.userID,
            "fileName" to fileName
        ))

        Connection.sendAudioWithJsonPostRequest(
            Connection.UPLOAD_TO_PUBLIC_COUGH,
            jsonData,
            dataArray,
            onRequestSuccess = {
                runOnUiThread {
                    Toast.makeText(this, "$fileName upload successfully as cough", Toast.LENGTH_SHORT).show()
                }
            },
            onRequestFail = {
                runOnUiThread {
                    Toast.makeText(this, "upload failed", Toast.LENGTH_SHORT).show()
                }
            },
            onConnectionFail = {
                runOnUiThread {
                    Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show()
                }
            },
            onFinish = {
                runOnUiThread {
                    processingMask.isVisible = false
                }
            }

        )
    }
    private fun sendCreateMusicAudioRequest(dataArray: ByteArray, fileName: String){
        val jsonData = Gson().toJson(mapOf(
            "userId" to GoogleService.userID,
            "fileName" to fileName
        ))

        Connection.sendAudioWithJsonPostRequest(
            Connection.UPLOAD_TO_PUBLIC_MUSIC,
            jsonData,
            dataArray,
            onRequestSuccess = {
                runOnUiThread {
                    Toast.makeText(this, "$fileName upload successfully as music", Toast.LENGTH_SHORT).show()
                }
            },
            onRequestFail = {
                runOnUiThread {
                    Toast.makeText(this, "upload failed", Toast.LENGTH_SHORT).show()
                }
            },
            onConnectionFail = {
                runOnUiThread {
                    Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show()
                }
            },
            onFinish = {
                runOnUiThread {
                    processingMask.isVisible = false
                }
            }

        )
    }


}
