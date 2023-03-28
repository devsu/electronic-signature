package com.demo.electronicsignature.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.demo.electronicsignature.data.database.converter.DateConverter
import com.demo.electronicsignature.data.database.dao.SignatureFileDao
import com.demo.electronicsignature.data.database.model.SignatureFile

@Database(entities = [SignatureFile::class], version = 2, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class MyDatabase :RoomDatabase() {
			abstract fun signatureFileDao(): SignatureFileDao
}