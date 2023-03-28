package com.demo.electronicsignature.data.database.dao

import androidx.room.*
import com.demo.electronicsignature.data.database.model.SignatureFile

@Dao
interface SignatureFileDao {

	@Query("SELECT * FROM signature_file")
	suspend fun getAll(): List<SignatureFile>

	@Query("SELECT * FROM signature_file WHERE id = :id")
	suspend fun getById(id: Int): SignatureFile

	@Insert(onConflict = OnConflictStrategy.ABORT)
	suspend fun insert(signatureFile: SignatureFile)

	@Delete
	suspend fun delete(signatureFile: SignatureFile)

}