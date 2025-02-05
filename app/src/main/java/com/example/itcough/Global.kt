package com.example.itcough

import android.net.Uri
import java.net.URL

// v4.1

object Global {
    //public val url = "http://172.20.10.2:8002"
    //const val URL = "http://192.168.1.108:8002"
    //public val url = "http://192.168.148.6:8002"
    //public val url = "http://192.168.137.1:8002"
    //const val URL = "http://140.114.207.214:8002"
    const val URL = "http://172.20.10.2:8002"

    var isRecording = false
    var isDetected = false

    var bass = "Tuba"
    var alto = "Viola"
    var high = "Violin"
    var userEmail: String? = null
    var userID: String? = null
    var userPhotoUrl: Uri? = null
    var userName: String? = null
    var maxCoughAudioLength: Int = 10
    var coughSpacingLength: Int = 2

}