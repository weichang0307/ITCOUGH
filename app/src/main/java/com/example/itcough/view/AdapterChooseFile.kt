package com.example.itcough.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.itcough.OnItemClickListener
import com.example.itcough.model.AudioRecord
import com.example.itcough.R
import kotlin.collections.ArrayList

class AdapterChooseFile(var listener: OnItemClickListener) : RecyclerView.Adapter<AdapterChooseFile.ViewHolder>() {
    public val records: ArrayList<AudioRecord> = ArrayList()
    public val allrecords: ArrayList<AudioRecord> = ArrayList()


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {
        init {
            // 将点击事件和长按事件绑定到 itemView
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
        var tvFilename: TextView = itemView.findViewById(R.id.tvFilename)
        override fun onClick(p0: View?) {
            Log.d("myTag","kkk")
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClickListener(position)
            }
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemLongClickListener(position)
            }
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_choose_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return records.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("myTag", "ggd")
        if (position != RecyclerView.NO_POSITION) {
            var record = records[position]
            holder.tvFilename.text = record.filename

        }
    }

    fun updateListener(listenerNew: OnItemClickListener) {
        listener = listenerNew
    }

}