package com.example.itcough

import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itcough.model.AudioRecord
import com.example.itcough.`object`.Connection
import com.example.itcough.`object`.Global
import com.example.itcough.`object`.GoogleService
import com.example.itcough.view.Adapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.collections.ArrayList


class CoughActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var records : ArrayList<AudioRecord>
    private lateinit var allrecords : ArrayList<AudioRecord>
    private lateinit var mAdapter: Adapter
    private lateinit var recyclerview: RecyclerView
    private lateinit var searchInput: TextInputEditText



    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var tvTitle: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cough)

        val topBarLayout = findViewById<View>(R.id.topBarLayout)
        btnLeft = topBarLayout.findViewById(R.id.btnLeft)
        tvTitle = topBarLayout.findViewById(R.id.tvTitle)
        btnRight = topBarLayout.findViewById(R.id.btnRight)
        btnRight.setBackgroundResource(R.drawable.ic_renew)
        btnLeft.setOnClickListener {
            finish()
        }

        btnRight.setOnClickListener {
            fetchAll()
        }



        records = ArrayList()
        allrecords = ArrayList()


        mAdapter = Adapter(records, this)

        recyclerview = findViewById(R.id.recyclerview)

        recyclerview.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)

        }


        searchInput = findViewById(R.id.search_input)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                var query = p0.toString()
                searchDatabase(query)
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        fetchAll()
    }



    private fun searchDatabase(query: String) {
        records.clear()
        var queryResult = allrecords.filter { it.filename.contains(query) }
        records.addAll(queryResult)
        runOnUiThread{
            mAdapter.notifyDataSetChanged()
        }
    }



    private fun fetchAll(){
        sendGetRecordsRequest("${Global.URL}/get_coughs/")

    }







    override fun onItemClickListener(position: Int) {
        val record = records[position]
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra("FILE_PATH", record.filePath) // 假設 record.filePath 是正確的音頻文件路徑
        intent.putExtra("FILE_NAME", record.filename) // 假設 record.filePath 是正確的音頻文件路徑
        startActivity(intent)

    }
    override fun onItemLongClickListener(position: Int) {}

    private fun sendGetRecordsRequest(urlString: String) {

        Thread {
            val url = URL(urlString)
            var connection: HttpURLConnection? = null
            try {
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    connectTimeout = 5000
                    readTimeout = 5000
                    doInput = true
                    doOutput = true // 允許輸出
                    setRequestProperty("Content-Type", "application/json") // 設置請求類型為 JSON
                }

                // 創建要傳輸的 JSON 數據
                val jsonPayload = """{"userId": "${GoogleService.userID}"}"""
                val outputStream = connection.outputStream
                outputStream.write(jsonPayload.toByteArray())
                outputStream.flush()
                outputStream.close()

                // 讀取響應
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                    Log.d("myTest", "Request was successful")
                    Log.d("myTest", parseAudioRecords(response)[0].filePath)
                    runOnUiThread {
                        allrecords.clear()
                        allrecords.addAll(parseAudioRecords(response))
                        searchInput.setText("")
                        searchDatabase("")
                    }
                } else {
                    Log.d("myTag", "Request failed with response code: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("myTag", "Error sending POST request", e)
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
}