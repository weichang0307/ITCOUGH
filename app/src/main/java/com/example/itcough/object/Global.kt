package com.example.itcough.`object`

// v4.1

object Global {
    //public val url = "http://172.20.10.2:8002"
    //const val URL = "http://192.168.1.108:8002"
    //public val url = "http://192.168.148.6:8002"
    //public val url = "http://192.168.137.1:8002"
    //const val URL = "http://140.114.207.214:8002"
    //const val URL = "http://172.20.10.4:8002"
    //const val URL = "http://192.168.0.217:8002"
    const val URL = "http://140.114.30.94:8002"//公網
    //const val URL = "http://140.114.207.214:8002"//宿網
    //const val URL = "http://192.168.0.253:8002"//實驗室
    //const val URL = "http://172.20.10.2:8002"


    var isRecording = false
    var isPublish = false
    var isDetected = false

    var detectStartTime: Long = 0

    var bass = "Tuba"
    var alto = "Viola"
    var high = "Violin"

    var maxCoughAudioLength: Int = 10
    var coughSpacingLength: Int = 2



}