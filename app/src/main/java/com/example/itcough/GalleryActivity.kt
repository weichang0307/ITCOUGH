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


class GalleryActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var records : ArrayList<AudioRecord>
    private lateinit var allrecords : ArrayList<AudioRecord>
    private lateinit var mAdapter: Adapter
    private lateinit var recyclerview: RecyclerView

    private var allChecked = false

    private lateinit var editBar: View
    private lateinit var btnClose: ImageButton
    private lateinit var btnSelectAll: ImageButton

    private lateinit var searchInput: TextInputEditText

    private lateinit var bottomSheet: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var btnRename : ImageButton
    private lateinit var btnDelete : ImageButton
    private lateinit var tvRename : TextView
    private lateinit var tvDelete : TextView

    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var tvTitle: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

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


        btnRename = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
        tvRename = findViewById(R.id.tvEdit)
        tvDelete = findViewById(R.id.tvDelete)


        editBar = findViewById(R.id.editBar)
        btnClose = findViewById(R.id.btnClose)
        btnSelectAll = findViewById(R.id.btnSelectAll)

        bottomSheet = findViewById(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        records = ArrayList()
        allrecords = ArrayList()


        mAdapter = Adapter(records, this)

        recyclerview = findViewById(R.id.recyclerview)

        recyclerview.apply{
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)

        }


        searchInput = findViewById(R.id.search_input)
        searchInput.addTextChangedListener(object: TextWatcher{
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

        btnClose.setOnClickListener {
            leaveEditMode()
        }

        btnSelectAll.setOnClickListener{
            allChecked = !allChecked
            records.map { it.isChecked = allChecked }
            mAdapter.notifyDataSetChanged()
            var nbSelected = records.count { it.isChecked }
            when (nbSelected) {
                0 -> {
                    disableRename()
                    disableDelete()
                }
                1 -> {
                    enableDelete()
                    enableRename()
                }
                else -> {
                    disableRename()
                    enableDelete()
                }
            }

        }
        btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete record?")
            val nbRecords = records.count{it.isChecked}
            builder.setMessage("Are you sure you want to delete $nbRecords record(s) ?")

            builder.setPositiveButton("Delete") {_,_->

                sendDeletePostRequest()
                leaveEditMode()
            }
            builder.setNegativeButton("Cancel") {_,_->}
            val dialog = builder.create()
            dialog.show()

        }

        btnRename.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = this.layoutInflater.inflate(R.layout.rename_layout, null)
            builder.setView(dialogView)
            val dialog = builder.create()

            val record = records.filter { it.isChecked }.get(0)
            val textInput = dialogView.findViewById<TextInputEditText>(R.id.filenameInput)
            textInput.setText((record.filename))

            dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
                val input = textInput.text.toString()
                if(input.isEmpty()){
                    Toast.makeText(this,"A name is required", Toast.LENGTH_LONG).show()
                }else{

                    sendRenamePostRequest(record, input, dialog)
                    dialog.dismiss()
                }
                mAdapter.notifyDataSetChanged()
            }

            dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()

            }

            dialog.show()

        }


    }



    private fun leaveEditMode(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        editBar.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        records.map{ it.isChecked = false }
        mAdapter.setEditMode(false)
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
        sendGetRecordsRequest()

    }






    private fun disableRename (){
        btnRename.isClickable = false
        btnRename.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.grayDarkDisabled,theme)
        tvRename.setTextColor(ResourcesCompat.getColorStateList(resources, R.color.grayDarkDisabled,theme))
    }

    private fun disableDelete (){
        btnDelete.isClickable = false
        btnDelete.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.grayDarkDisabled,theme)
        tvDelete.setTextColor(ResourcesCompat.getColorStateList(resources, R.color.grayDarkDisabled,theme))
    }

    private fun enableRename (){
        btnRename.isClickable = true
        btnRename.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.grayDark,theme)
        tvRename.setTextColor(ResourcesCompat.getColorStateList(resources, R.color.grayDark,theme))
    }

    private fun enableDelete (){
        btnDelete.isClickable = true
        btnDelete.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.grayDark,theme)
        tvDelete.setTextColor(ResourcesCompat.getColorStateList(resources, R.color.grayDark,theme))
    }

    override fun onItemClickListener(position: Int) {
        val record = records[position]
        if (mAdapter.isEditMode()) {
            record.isChecked = !record.isChecked
            mAdapter.notifyItemChanged(position)

            var nbSelected = records.count { it.isChecked }
            when (nbSelected) {
                0 -> {
                    leaveEditMode()
                }
                1 -> {
                    enableDelete()
                    enableRename()
                }
                else -> {
                    disableRename()
                    enableDelete()
                }
            }
        }else{
            val intent = Intent(this, AudioPlayerActivity::class.java)
            intent.putExtra("FILE_PATH", record.filePath) // 假設 record.filePath 是正確的音頻文件路徑
            intent.putExtra("FILE_NAME", record.filename) // 假設 record.filePath 是正確的音頻文件路徑
            startActivity(intent)
        }
    }
    override fun onItemLongClickListener(position: Int) {
        mAdapter.setEditMode(true)
        records[position].isChecked = !records[position].isChecked
        mAdapter.notifyItemChanged(position)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        if(mAdapter.isEditMode() && editBar.visibility == View.GONE ){
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)

            editBar.visibility = View.VISIBLE

            enableDelete()
            enableRename()

        }
        var nbSelected = records.count { it.isChecked }
        when (nbSelected) {
            0 -> {
                disableRename()
                disableDelete()
            }
            1 -> {
                enableDelete()
                enableRename()
            }
            else -> {
                disableRename()
                enableDelete()
            }
        }
    }

    override fun finish() {
        if(mAdapter.isEditMode()){
            leaveEditMode()
        }
        else{
            super.finish()
        }
    }
    private fun sendRenamePostRequest(record: AudioRecord, name: String, dialog: AlertDialog) {
        val jsonData = Gson().toJson(mapOf(
            "userId" to GoogleService.userID,
            "oldPath" to record.filePath,
            "oldName" to record.filename,
            "name" to name
        ))
        Connection.sendJsonPostRequest(
            Connection.RENAME_MUSIC_PATH,
            jsonData,
            onRequestSuccess = {
                runOnUiThread {
                    leaveEditMode()
                    Toast.makeText(this@GalleryActivity, "Successful to rename as \"${name}\"", Toast.LENGTH_LONG).show()
                    sendGetRecordsRequest()
                }
            },
            onRequestFail = {
                Toast.makeText(this@GalleryActivity, "Fail to rename \"${record.filename}\"", Toast.LENGTH_LONG).show()
            },
            onConnectionFail = {
                Toast.makeText(this@GalleryActivity, "Fail to connect to server", Toast.LENGTH_LONG).show()
            }
        )
    }
    private fun sendDeletePostRequest() {
        val toDelete = records.filter {it.isChecked}.toTypedArray()
        val jsonData = Gson().toJson(mapOf(
            "userId" to GoogleService.userID,
            "targetList" to toDelete
        ))
        Connection.sendJsonPostRequest(
            Connection.DELETE_MUSIC_PATH,
            jsonData,
            onRequestSuccess = {
                Log.d("myTag", "Request was successful")
                runOnUiThread{
                    sendGetRecordsRequest()
                }
            },
            onRequestFail = {
                runOnUiThread {
                    Toast.makeText(this@GalleryActivity, "Fail to delete ${toDelete.size} record(s)", Toast.LENGTH_LONG).show()
                }
            },
            onConnectionFail = {
                runOnUiThread {
                    Toast.makeText(this@GalleryActivity, "Fail to connect to sever", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
    private fun sendGetRecordsRequest() {
        val jsonData = """{"userId": "${GoogleService.userID}"}"""
        Connection.sendJsonPostRequest(
            Connection.GET_MUSIC_LIST_PATH,
            jsonData,
            onRequestSuccess = { connection ->
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                Log.d("myTest", "Request was successful")
                Log.d("myTest", parseAudioRecords(response).toString())
                runOnUiThread {
                    allrecords.clear()
                    allrecords.addAll(parseAudioRecords(response))
                    searchInput.setText("")
                    searchDatabase("")
                }
            }
        )
    }

    private fun parseAudioRecords(jsonString: String): List<AudioRecord> {
        val gson = Gson()
        val listType = object : TypeToken<List<AudioRecord>>() {}.type
        return gson.fromJson(jsonString, listType)
    }
}