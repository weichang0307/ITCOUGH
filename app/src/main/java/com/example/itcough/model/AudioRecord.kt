package com.example.itcough.model


data class AudioRecord (
    var filename: String,
    var filePath: String,
    var timestamp: Long,
    var duration: String
){
    var isChecked = false
}