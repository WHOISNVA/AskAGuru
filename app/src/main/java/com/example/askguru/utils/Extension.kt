package com.example.askguru.utils

fun Long.getLongToSeconds(): Int {
    return (this / (1000 * 60)).toInt()
}