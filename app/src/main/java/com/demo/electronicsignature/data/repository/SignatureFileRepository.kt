package com.demo.electronicsignature.data.repository

import com.demo.electronicsignature.data.database.model.SignatureFile
import java.io.File

interface SignatureFileRepository {
	suspend fun saveFile(path: String)
	suspend fun listAll(): List<SignatureFile>
	suspend fun deleteFile(it: SignatureFile)
}