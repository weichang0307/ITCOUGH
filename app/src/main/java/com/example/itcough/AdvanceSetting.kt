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

class AdvanceSetting : ComponentActivity() {

    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var btnChooseBass: EditText
    private lateinit var btnChooseAlto: EditText
    private lateinit var btnChooseHigh: EditText
    private val bass_instruments = listOf(
        "Tuba",
        "Double Bass",
        "Bassoon",
        "Trombone"
    )
    private val alto_instruments = listOf(
        "Viola",
        "Cello",
        "Horn",
        "Clarinet",
        "Saxophone",
        "Trombone"
    )
    private val high_instruments = listOf(
        "Violin",
        "Flute",
        "Oboe",
        "Trumpet",
        "Guitar"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advance_setting)
        val topBarLayout = findViewById<View>(R.id.topBarLayout)
        btnLeft = topBarLayout.findViewById(R.id.btnLeft)
        btnRight = topBarLayout.findViewById(R.id.btnRight)
        btnChooseBass = findViewById(R.id.btnChooseBass)
        btnChooseAlto = findViewById(R.id.btnChooseAlto)
        btnChooseHigh = findViewById(R.id.btnChooseHigh)
        btnLeft.setOnClickListener {
            finish()
        }
        btnRight.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
        btnRight.visibility = View.INVISIBLE
        btnChooseBass.setOnClickListener() {
            showMenuDialog(bass_instruments, btnChooseBass, "bass")
        }
        btnChooseAlto.setOnClickListener() {
            showMenuDialog(alto_instruments, btnChooseAlto, "alto")
        }
        btnChooseHigh.setOnClickListener() {
            showMenuDialog(high_instruments, btnChooseHigh, "high")
        }
        btnChooseBass.setText(Global.bass)
        btnChooseAlto.setText(Global.alto)
        btnChooseHigh.setText(Global.high)

    }

    private fun showMenuDialog(list: List<String>, view: EditText, type: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.list_layout, null)
        val listView: ListView = dialogView.findViewById(R.id.listView)

        val adapter = object : ArrayAdapter<String>(this, R.layout.itemview_choose_layout, list) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.itemview_choose_layout, parent, false)
                val filenameText = view.findViewById<TextView>(R.id.tvFilename)
                filenameText.text = list[position]
                return view
            }
        }
        listView.adapter = adapter

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setNegativeButton("Cancel", null)
        val dialog = builder.create()

        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setDimAmount(0.8f)
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            view.setText(list[position])
            if (type == "bass"){
                Global.bass = list[position]
            }
            if (type == "alto"){
                Global.alto = list[position]
            }
            if (type == "high"){
                Global.high = list[position]
            }
            dialog.dismiss()
        }

        dialog.show()
    }

}
