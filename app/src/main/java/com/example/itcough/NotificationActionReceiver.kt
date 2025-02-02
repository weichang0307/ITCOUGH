package com.example.itcough

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.itcough.model.ContinuousAudioRecorder

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            "ACTION_STOP_RECORDING" -> {
                Log.d("NotificationReceiver", "停止錄音")
                context.stopService(Intent(context, ContinuousAudioRecorder::class.java))
            }
        }
    }
}
