package com.example.bluetoothapp.utils

fun ByteArray.toHex() = joinToString("") { String.format("%02X", (it.toInt() and 0xff)) }