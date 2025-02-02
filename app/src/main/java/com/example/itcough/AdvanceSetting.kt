package com.example.itcough

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.itcough.ui.theme.ITCOUGHTheme

class AdvanceSetting : ComponentActivity() {
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
    }

    private fun showMenuDialog(list: List<String>, view: EditText) {
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
            dialog.dismiss()
        }

        dialog.show()
    }
}
