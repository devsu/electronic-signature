package com.demo.electronicsignature.domain

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.electronicsignature.App
import com.demo.electronicsignature.R
import com.demo.electronicsignature.data.repository.SignatureFileRepository
import com.demo.electronicsignature.domain.model.NewSignature
import com.demo.electronicsignature.domain.model.Signature
import com.demo.electronicsignature.domain.model.SignatureData
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class SignatureListViewModel(
	private val signatureFileRepository: SignatureFileRepository = App.signatureFileRepository,
) : ViewModel() {

	private val _filesList: MutableLiveData<List<Signature>> = MutableLiveData()
	val filesList: LiveData<List<Signature>> = _filesList

	private val firstElement = NewSignature(R.string.new_signature_instruction)

	private val _filesDeleted: MutableLiveData<Int> = MutableLiveData()
	val filesDeleted: LiveData<Int> = _filesDeleted


	fun loadDatabase() {
		var signData: List<Signature?>
		val deletedFiles = 0

		viewModelScope.launch {
			val files = signatureFileRepository.listAll()
			Log.i("SignatureListViewModel", "files: ${files.size}")
			signData = files.map {
				val file = File(it.path)
				if (!file.exists()) {
					signatureFileRepository.deleteFile(it)
					deletedFiles.plus(1)
					return@map null
				}
				SignatureData(file.name, it.registrationDate, file.toUri())
			}
			_filesList.postValue(signData.filterNotNull().toMutableList().apply { add(0, firstElement) })
			_filesDeleted.postValue(deletedFiles)
		}
	}

	fun addFile(uri: Uri) {
		Log.i("SignatureListViewModel", "uri: $uri")
		val path = uri.path.toString().split(":").last()
		Log.i("SignatureListViewModel", "path: $path")
		val file = File(path)
		if(!file.exists()) return
		viewModelScope.launch {
			signatureFileRepository.saveFile(file.absolutePath)
		}
		val list = _filesList.value?.toMutableList() ?: mutableListOf()
		list.add(SignatureData(file.name, Date(), file.absolutePath.toUri()))
		_filesList.value = list
	}
}
