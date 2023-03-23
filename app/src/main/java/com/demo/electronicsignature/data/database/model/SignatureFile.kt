package com.demo.electronicsignature.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.demo.electronicsignature.data.database.converter.DateConverter
import java.util.Date


@Entity(tableName = "signature_file")
data class SignatureFile(
	@PrimaryKey(autoGenerate = true)
	var id: Int = 0,
	var path: String,
	var registrationDate: Date = Date()
)