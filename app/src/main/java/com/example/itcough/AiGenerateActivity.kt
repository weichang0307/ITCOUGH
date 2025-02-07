package com.example.itcough

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itcough.model.AudioRecord
import com.example.itcough.`object`.Connection
import com.example.itcough.`object`.Global
import com.example.itcough.`object`.GoogleService
import com.example.itcough.view.AdapterChooseFile
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import java.io.BufferedReader
import java.util.UUID

class AiGenerateActivity : ComponentActivity(), OnItemClickListener {

    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var btnChooseCough: EditText
    private lateinit var btnGenerate: Button
    private lateinit var btnSave: Button
    private lateinit var btnAdvanceSetting: Button
    private lateinit var adapterChooseCoughFile: AdapterChooseFile
    private lateinit var dialogChooseCoughFile: AlertDialog
    private lateinit var searchInput: TextInputEditText
    private var coughRecord: AudioRecord? = null


    private var generatePath: String? = ""
    private var temperUUID: String? = null
    private var isGenerate = false
    private var isGenerating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_generate)
        val topBarLayout = findViewById<View>(R.id.topBarLayout)
        btnLeft = topBarLayout.findViewById(R.id.btnLeft)
        btnRight = topBarLayout.findViewById(R.id.btnRight)
        btnChooseCough = findViewById(R.id.btnChooseCough)
        btnGenerate = findViewById(R.id.btnGenerate)
        btnSave = findViewById(R.id.btnSave)
        btnAdvanceSetting = findViewById(R.id.btnAdvanceSetting)

        switchToSettingMode()

        btnLeft.setOnClickListener {
            actionConfirm(){finish()}
        }
        btnRight.setBackgroundResource(R.drawable.ic_renew)
        btnRight.setOnClickListener {
            actionConfirm(){refreshUI()}
        }
        btnChooseCough.setOnClickListener(){
            showChooseCoughDialog()
            sendGetCoughAudioListRequest()
        }
        btnAdvanceSetting.setOnClickListener(){
            val intent = Intent(this, AdvanceSetting::class.java)
            startActivity(intent)
        }
        btnGenerate.setOnClickListener(){
            if(btnGenerate.text == "Generate"){
                sendGeneratePostRequest()
            }else{
                val intent = Intent(this, AudioPlayerActivity::class.java)
                intent.putExtra("FILE_PATH", generatePath) // 假設 record.filePath 是正確的音頻文件路徑
                intent.putExtra("FILE_NAME", "") // 假設 record.filePath 是正確的音頻文件路徑
                startActivity(intent)
            }

        }
        btnSave.setOnClickListener(){
            if (isGenerate) showSaveFileDialog(this)
        }




    }

    override fun onDestroy() {
        super.onDestroy()
        sendCleanTemperRequest()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            actionConfirm(){finish()}
            return true;
        }

        return super.onKeyDown(keyCode, event)
    }
    override fun onItemClickListener(position: Int) {
        runOnUiThread{
            coughRecord = adapterChooseCoughFile.records[position]
            btnChooseCough.setText(coughRecord?.filename.toString())
            adapterChooseCoughFile.records.clear()
            dialogChooseCoughFile.dismiss()
        }
    }
    override fun onItemLongClickListener(position: Int) {
        val record = adapterChooseCoughFile.records[position]
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra("FILE_PATH", record.filePath)
        intent.putExtra("FILE_NAME", record.filename)
        startActivity(intent)
    }

    private fun getGenerateData(): Map<String, String>? {
        if(btnChooseCough.text.isEmpty()) {
            Toast.makeText(this@AiGenerateActivity, "Select a cough audio", Toast.LENGTH_LONG).show()
            return null
        }
        temperUUID = UUID.randomUUID().toString()
        switchToGeneratingMode()
        return mapOf(
            "cough_path" to coughRecord?.filePath.toString(),
            "user_id" to GoogleService.userID.toString(),
            "alto" to Global.alto,
            "bass" to Global.bass,
            "high" to Global.high,
            "uuid" to temperUUID.toString()
        )
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun sendGetCoughAudioListRequest() {
        val jsonData = Gson().toJson(mapOf(
            "userId" to GoogleService.userID
        ))
        Connection.sendJsonPostRequest(
            Connection.GET_COUGH_AUDIO_LIST_PATH,
            jsonData,
            onRequestSuccess = { connection ->
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                runOnUiThread {
                    adapterChooseCoughFile.allrecords.clear()
                    adapterChooseCoughFile.allrecords.addAll(Connection.parseAudioRecords(response))
                    searchInput.setText("")
                    searchDatabase("")
                    adapterChooseCoughFile.notifyDataSetChanged()
                }

            }
        )
    }
    private fun sendGeneratePostRequest() {
        val generateData = getGenerateData() ?: return
        val jsonData = Gson().toJson(generateData)
        Connection.sendJsonPostRequest(
            Connection.GENERATE_MUSIC_PATH,
            jsonData,
            onRequestSuccess = { connection ->
                val responseJson = connection.inputStream.bufferedReader().use { it.readText() }
                generatePath = Connection.jsonToMap(responseJson)["generate_path"]
                runOnUiThread{
                    switchToBrowsingMode()
                }
            },
            onRequestFail = {
                runOnUiThread{
                    switchToSettingMode()
                }
            },
            onConnectionFail = {
                runOnUiThread {
                    switchToSettingMode()
                }
            }

        )
    }
    private fun sendSaveMusicRequest(fileName: String){
        if (!isGenerate) return
        if (temperUUID == null) return
        val jsonData = Gson().toJson(mapOf(
            "userId" to GoogleService.userID,
            "uuid" to temperUUID,
            "fileName" to fileName
        ))
        Connection.sendJsonPostRequest(
            Connection.SAVE_GENERATED_MUSIC_PATH,
            jsonData,
            onRequestSuccess = {
                runOnUiThread{
                    Toast.makeText(this@AiGenerateActivity, "Music Successfully Saved", Toast.LENGTH_SHORT).show()
                    refreshUI()
                }
            }
        )
    }
    private fun sendCleanTemperRequest(){
        val jsonData = Gson().toJson(mapOf(
            "userId" to GoogleService.userID,
        ))
        Connection.sendJsonPostRequest(
            Connection.CLEAN_TEMPER_PATH,
            jsonData
        )
    }
    private fun showChooseCoughDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_choose_file, null)
        val recyclerview = dialogView.findViewById<RecyclerView>(R.id.recyclerview)
        searchInput = dialogView.findViewById<TextInputEditText>(R.id.search_input)
        searchInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val query = p0.toString()
                searchDatabase(query)
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        recyclerview.layoutManager = LinearLayoutManager(this)
        adapterChooseCoughFile = AdapterChooseFile(this)
        recyclerview.adapter = adapterChooseCoughFile
        dialogChooseCoughFile = builder.setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        dialogChooseCoughFile.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent) // 设置背景透明
            setDimAmount(0.8f) // 设置背景遮罩透明度
        }
        dialogChooseCoughFile.show()
    }

    private fun showSaveFileDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Save Generated Music")
        // 創建 EditText 讓使用者輸入
        val input = EditText(context)
        input.hint = "input file name"

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)
        layout.addView(input)
        builder.setView(layout)
        // 設置按鈕
        builder.setPositiveButton("save") { _, _ ->
            val fileName = input.text.toString().trim()
            if (fileName.isNotEmpty()) {
                sendSaveMusicRequest(fileName)
            } else {
                Toast.makeText(context, "file name can not be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
    private fun showDiscardFileDialog(context: Context, onDiscard: () -> Unit) {
        AlertDialog.Builder(context)
            .setMessage("There is an unsaved file")
            .setNegativeButton("Discard") { _, _ ->
                onDiscard() // 用户确认放弃时调用
            }
            .setPositiveButton("Save") { dialog, _ ->
                dialog.dismiss() // 关闭对话框
                showSaveFileDialog(context) // 弹出保存文件的对话框
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss() // 只关闭对话框，什么也不做
            }
            .show()
    }
    private fun showCancelGenerationDialog(context: Context, onDiscard: () -> Unit) {
        AlertDialog.Builder(context)
            .setMessage("You want to discard the generating process?")
            .setNegativeButton("Discard") { _, _ ->
                onDiscard() // 用户确认放弃时调用
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss() // 只关闭对话框，什么也不做
            }
            .show()
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun searchDatabase(query: String) {
        adapterChooseCoughFile.records.clear()
        val queryResult = adapterChooseCoughFile.allrecords.filter { it.filename.contains(query) }
        adapterChooseCoughFile.records.addAll(queryResult)
        runOnUiThread{
            adapterChooseCoughFile.notifyDataSetChanged()
        }
    }
    private fun refreshUI(){
        if (isGenerate) sendCleanTemperRequest()
        temperUUID = null
        btnChooseCough.setText("")
        switchToSettingMode()
    }
    private fun actionConfirm(action: () -> Unit) {
        if (isGenerate){
            showDiscardFileDialog(this){
                action()
            }
        }else if (isGenerating){
            showCancelGenerationDialog(this){
                action()
            }
        }else{
            action()
        }
    }
    private fun switchToSettingMode() {
        btnAdvanceSetting.isVisible = true
        btnGenerate.isClickable = true
        btnGenerate.text = getString(R.string.generate)
        btnSave.isVisible = false
        btnChooseCough.isEnabled = true
        isGenerate = false
        isGenerating = false
    }
    private fun switchToGeneratingMode() {
        btnAdvanceSetting.isVisible = false
        btnGenerate.isClickable = false
        btnGenerate.text = getString(R.string.generating)
        btnSave.isVisible = false
        btnChooseCough.isEnabled = false
        isGenerate = false
        isGenerating = true
    }
    private fun switchToBrowsingMode() {
        btnAdvanceSetting.isVisible = false
        btnGenerate.isClickable = true
        btnGenerate.text = getString(R.string.play)
        btnSave.isVisible = true
        btnChooseCough.isEnabled = false
        isGenerate = true
        isGenerating = false
    }
}
