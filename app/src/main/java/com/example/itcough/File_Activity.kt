package com.example.itcough

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView

class File_Activity : ComponentActivity() {
    private lateinit var myMusic: CardView
    private lateinit var myCough: CardView
    private lateinit var myImport: CardView
    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)
        myMusic = findViewById(R.id.cardMyMusic)
        myCough = findViewById(R.id.cardMyCough)
        myImport = findViewById(R.id.cardImport)
        myMusic.setOnClickListener{
            startActivity(Intent(this, GalleryActivity::class.java))
        }
        myCough.setOnClickListener{
            startActivity(Intent(this, CoughActivity::class.java))
        }
        myImport.setOnClickListener{
            startActivity(Intent(this, ImportActivity::class.java))
        }
        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)
        btnLeft.setOnClickListener {
            finish()
        }
        btnRight.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
    }

}
