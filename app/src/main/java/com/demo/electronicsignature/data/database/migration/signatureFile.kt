package com.demo.electronicsignature.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val migration_1_2 = object : Migration(1, 2) {
		override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("ALTER TABLE signature_file ADD COLUMN registrationDate INTEGER NOT NULL DEFAULT 0")
		}
}