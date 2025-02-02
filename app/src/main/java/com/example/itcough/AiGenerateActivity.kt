package com.example.itcough

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itcough.model.AudioRecord
import com.example.itcough.view.AdapterChooseFile
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class AiGenerateActivity : ComponentActivity(), OnItemClickListener {

    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var btnChooseCough: EditText
    private lateinit var btnGenerate: Button
    private lateinit var btnAdvanceSetting: Button
    private lateinit var adapter: AdapterChooseFile
    private lateinit var dialog: AlertDialog
    private lateinit var searchInput: TextInputEditText
    private var coughRecord: AudioRecord? = null

    private var generatePath: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_generate)
        val topBarLayout = findViewById<View>(R.id.topBarLayout)
        btnLeft = topBarLayout.findViewById(R.id.btnLeft)
        btnRight = topBarLayout.findViewById(R.id.btnRight)
        btnChooseCough = findViewById(R.id.btnChooseCough)
        btnGenerate = findViewById(R.id.btnGenerate)
        btnAdvanceSetting = findViewById(R.id.btnAdvanceSetting)
        btnLeft.setOnClickListener {
            finish()
        }
        btnRight.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
        btnRight.setBackgroundResource(R.drawable.ic_renew)
        btnRight.setOnClickListener {
            btnGenerate.isClickable = true
            btnGenerate.text = "Generate"
            btnChooseCough.isEnabled = true
            btnChooseCough.setText("")
        }
        btnChooseCough.setOnClickListener(){
            dialog.show()
            fetchAll()
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



        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_choose_file, null)
        val recyclerview = dialogView.findViewById<RecyclerView>(R.id.recyclerview)
        searchInput = dialogView.findViewById<TextInputEditText>(R.id.search_input)
        recyclerview.layoutManager = LinearLayoutManager(this)
        adapter = AdapterChooseFile(this)
        recyclerview.adapter = adapter
        dialog = builder.setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent) // 设置背景透明
            setDimAmount(0.8f) // 设置背景遮罩透明度
        }
        searchInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                var query = p0.toString()
                searchDatabase(query)
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })



    }
    private fun fetchAll(){
        sendGetRecordsRequest("${Global.URL}/get_records/")

    }
    @SuppressLint("NotifyDataSetChanged")
    private fun sendGetRecordsRequest(urlString: String) {
        Thread {
            val url = URL(urlString)
            var connection: HttpURLConnection? = null
            try {
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                // 读取响应
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                    Log.d("myTest", "Request was successful")
                    Log.d("myTest", parseAudioRecords(response)[0].filePath)

                    runOnUiThread {
                        adapter.allrecords.clear()
                        adapter.allrecords.addAll(parseAudioRecords(response))
                        searchInput.setText("")
                        searchDatabase("")
                        Log.d("myTag", adapter.records.toString())
                        adapter.notifyDataSetChanged()
                    }

                } else {
                    Log.d("myTag", "Request failed with response code: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("myTag", "Error sending GET request", e)
            } finally {
                connection?.disconnect()
            }
        }.start()
    }
    private fun parseAudioRecords(jsonString: String): List<AudioRecord> {
        val gson = Gson()
        val listType = object : TypeToken<List<AudioRecord>>() {}.type
        return gson.fromJson(jsonString, listType)
    }

    override fun onItemClickListener(position: Int) {
        runOnUiThread{
            coughRecord = adapter.records[position]
            btnChooseCough.setText(coughRecord?.filename.toString())
            adapter.records.clear()
            dialog.dismiss()
        }

    }
    override fun onItemLongClickListener(position: Int) {
        val record = adapter.records[position]
        Log.d("myTag","das")
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra("FILE_PATH", record.filePath)
        intent.putExtra("FILE_NAME", record.filename)
        startActivity(intent)
    }
    private fun searchDatabase(query: String) {
        adapter.records.clear()
        var queryResult = adapter.allrecords.filter { it.filename.contains(query) }
        adapter.records.addAll(queryResult)
        runOnUiThread{
            adapter.notifyDataSetChanged()
        }
    }
    private fun getGenerateData(): Map<String, String>? {
        if(btnChooseCough.text.length == 0) {
            Toast.makeText(this@AiGenerateActivity, "Select a cough audio", Toast.LENGTH_LONG).show()
            return null
        }

        return mapOf(
            "cough_path" to coughRecord?.filePath.toString()
        )
    }
    private fun sendGeneratePostRequest() {
        val generateData = getGenerateData() ?: return
        btnGenerate.isClickable = false
        btnGenerate.text = "Generating..."
        btnChooseCough.isEnabled = false
        val urlString = "${Global.URL}/generate/"
        Thread {
            // 创建 URL 对象
            val url = URL(urlString)

            // 打开连接
            val connection = url.openConnection() as HttpURLConnection

            try {
                // 设置请求方法为 POST
                connection.requestMethod = "POST"
                connection.connectTimeout = 5000
                connection.readTimeout = 500000

                // 设置请求头，表明我们发送的是 JSON 数据
                connection.setRequestProperty("Content-Type", "application/json")

                // 启用输出流，以便我们可以发送请求体数据
                connection.doOutput = true
                val jsonData = Gson().toJson(generateData)
                // 发送 JSON 数据
                connection.outputStream.use { outputStream ->
                    // 将 JSON 数据转换为字节并写入输出流
                    outputStream.write(jsonData.toByteArray())
                    outputStream.flush()
                }

                // 获取响应代码
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("myTag", "Request was successful")
                    val responseJson = connection.inputStream.bufferedReader().use { it.readText() }
                    generatePath = jsonToMap(responseJson)["generate_path"]
                    runOnUiThread{
                        btnGenerate.isClickable = true
                        btnGenerate.text = "Play"
                        Toast.makeText(this@AiGenerateActivity, "Generate was successful", Toast.LENGTH_LONG).show()
                    }


                } else {
                    Log.d("myTag", "Request failed with response code: $responseCode")

                }
            } catch (e: IOException) {
                Log.e("myTag", "Error sending request", e)
            } finally {
                connection.disconnect() // 关闭连接
            }
        }.start()
    }

    fun jsonToMap(jsonString: String): Map<String, String> {
        val jsonObject = JSONObject(jsonString)
        val map = mutableMapOf<String, String>()
        val iterator = jsonObject.keys()

        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = jsonObject.get(key).toString()
        }
        return map
    }

}
