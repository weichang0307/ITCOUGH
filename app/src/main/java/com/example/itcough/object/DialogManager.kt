package com.example.itcough.`object`

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.UiContext
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itcough.AudioPlayerActivity
import com.example.itcough.OnItemClickListener
import com.example.itcough.R
import com.example.itcough.model.AudioRecord
import com.example.itcough.view.AdapterChooseFile
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import java.io.BufferedReader

object DialogManager {
    fun showActionWithInputTextDialog(
        context: Activity,
        title: String,
        actionName: String,
        hint: String = "input file name",
        onSave: (DialogInterface, String) -> Unit = { _, _ -> },
        onCancel: (DialogInterface) -> Unit = {}
    ){
        val builder = AlertDialog.Builder(context)
        builder.setMessage(title)
        // 創建 EditText 讓使用者輸入
        val input = EditText(context)
        input.hint = hint

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)
        layout.addView(input)
        builder.setView(layout)
        // 設置按鈕
        builder.setPositiveButton(actionName) { dialog, _ ->
            onSave(dialog, input.text.toString())
        }
        builder.setNegativeButton("cancel") { dialog, _ ->
            onCancel(dialog)
        }

        builder.show()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun showChooseAudioDialog(
        context: Activity,
        title: String = "Save Audio",
        onOnItemClickListener: (AudioRecord) -> Unit = {},
        onOnItemLongClickListener: (AudioRecord) -> Unit = {},
    ) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_choose_file, null)
        val recyclerview = dialogView.findViewById<RecyclerView>(R.id.recyclerview)
        val searchInput = dialogView.findViewById<TextInputEditText>(R.id.search_input)
        val processingMask = dialogView.findViewById<FrameLayout>(R.id.processingMask)
        val textView = dialogView.findViewById<TextView>(R.id.textView)
        textView.text = title
        val adapterChooseCoughFile = AdapterChooseFile(object: OnItemClickListener {
            override fun onItemClickListener(position: Int) {
            }
            override fun onItemLongClickListener(position: Int) {
            }
        })
        val dialogChooseCoughFile: AlertDialog = builder.setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()
        val listener = object: OnItemClickListener {
            override fun onItemClickListener(position: Int) {
                val record = adapterChooseCoughFile.records[position]
                adapterChooseCoughFile.records.clear()
                dialogChooseCoughFile.dismiss()
                onOnItemClickListener(record)
            }

            override fun onItemLongClickListener(position: Int) {
                val record = adapterChooseCoughFile.records[position]
                val intent = Intent(context, AudioPlayerActivity::class.java)
                intent.putExtra("FILE_PATH", record.filePath)
                intent.putExtra("FILE_NAME", record.filename)
                context.startActivity(intent)
                onOnItemLongClickListener(record)
            }
        }
        adapterChooseCoughFile.updateListener(listener)
        searchInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val query = p0.toString()
                adapterChooseCoughFile.records.clear()
                val queryResult = adapterChooseCoughFile.allrecords.filter { it.filename.contains(query) }
                adapterChooseCoughFile.records.addAll(queryResult)
                adapterChooseCoughFile.notifyDataSetChanged()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        recyclerview.layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = adapterChooseCoughFile
        dialogChooseCoughFile.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent) // 设置背景透明
            setDimAmount(0.8f) // 设置背景遮罩透明度
        }
        processingMask.isVisible = true
        val jsonData = Gson().toJson(mapOf(
            "userId" to GoogleService.userID
        ))
        Connection.sendJsonPostRequest(
            Connection.GET_COUGH_AUDIO_LIST_PATH,
            jsonData,
            onRequestSuccess = { connection ->
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                context.runOnUiThread {
                    adapterChooseCoughFile.allrecords.clear()
                    adapterChooseCoughFile.allrecords.addAll(Connection.parseAudioRecords(response))
                    searchInput.setText("")
                    adapterChooseCoughFile.records.clear()
                    val queryResult = adapterChooseCoughFile.allrecords.filter { it.filename.contains("") }
                    adapterChooseCoughFile.records.addAll(queryResult)
                    adapterChooseCoughFile.notifyDataSetChanged()
                }
            },
            onRequestFail = {
                context.runOnUiThread {
                    Toast.makeText(context, "Fail to get audio list", Toast.LENGTH_SHORT).show()
                }
            },
            onConnectionFail = {
                context.runOnUiThread{
                    Toast.makeText(context, "Connection failure", Toast.LENGTH_SHORT).show()
                }
            },
            onFinish = {
                context.runOnUiThread {
                    processingMask.isVisible = false
                }
            }

        )
        dialogChooseCoughFile.show()
    }
}