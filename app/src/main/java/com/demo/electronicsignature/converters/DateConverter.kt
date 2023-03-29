package com.demo.electronicsignature.converters

import java.text.SimpleDateFormat
import java.util.*

fun Date.getFormattedDate(delimiter: String): String {
		val formatter = SimpleDateFormat("dd${delimiter}MM${delimiter}yyyy", Locale.getDefault())
		return formatter.format(this)
}

fun Date.getFormattedTime(delimiter: String): String {
		val formatter = SimpleDateFormat("HH${delimiter}mm${delimiter}ss", Locale.getDefault())
		return formatter.format(this)
}