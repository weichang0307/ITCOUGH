package com.example.itcough

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itcough.model.AudioRecord
import com.example.itcough.view.AdapterChooseFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class CreateActivity : ComponentActivity() {

    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var btnAiGenerate: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        val topBarLayout = findViewById<View>(R.id.topBarLayout)
        btnLeft = topBarLayout.findViewById(R.id.btnLeft)
        btnRight = topBarLayout.findViewById(R.id.btnRight)
        btnAiGenerate = findViewById(R.id.cardAiGenerate)
        btnLeft.setOnClickListener {
            finish()
        }
        btnRight.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
        btnAiGenerate.setOnClickListener {
            startActivity(Intent(this, AiGenerateActivity::class.java))
        }


    }

}
