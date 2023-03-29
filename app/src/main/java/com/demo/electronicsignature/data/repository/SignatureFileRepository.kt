package com.demo.electronicsignature.data.repository

import com.demo.electronicsignature.data.database.model.SignatureFile

interface SignatureFileRepository {
	suspend fun saveFile(path: String): Long
	suspend fun listAll(): List<SignatureFile>
	suspend fun deleteFile(it: SignatureFile)
}