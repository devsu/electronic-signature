package com.demo.electronicsignature.data.repository

import com.demo.electronicsignature.data.database.dao.SignatureFileDao
import com.demo.electronicsignature.data.database.model.SignatureFile

class SignatureFileRepositoryImpl(
	private val signatureFileDao: SignatureFileDao
) : SignatureFileRepository {
	override suspend fun saveFile(path: String): Long {
		return signatureFileDao.insert(SignatureFile(path = path))
	}

	override suspend fun listAll(): List<SignatureFile> {
		return signatureFileDao.getAll()
	}

	override suspend fun deleteFile(it: SignatureFile) {
		signatureFileDao.delete(it.id)
	}
}