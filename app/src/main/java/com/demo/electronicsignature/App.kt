package com.demo.electronicsignature

import android.app.Application
import androidx.room.Room
import com.demo.electronicsignature.data.database.MyDatabase
import com.demo.electronicsignature.data.database.migration.migration_1_2
import com.demo.electronicsignature.data.repository.SignatureFileRepositoryImpl

class App: Application() {

		override fun onCreate() {
				super.onCreate()
				context = this
		}

		companion object {
				lateinit var context: App

				private val roomDatabase by lazy {
						Room.databaseBuilder(
								context,
								MyDatabase::class.java,
								"database-signatures"
						)
							.addMigrations(migration_1_2)
							.build()
				}

				private val signatureFileDao by lazy { roomDatabase.signatureFileDao() }

			val signatureFileRepository by lazy { SignatureFileRepositoryImpl(signatureFileDao) }

		}
}