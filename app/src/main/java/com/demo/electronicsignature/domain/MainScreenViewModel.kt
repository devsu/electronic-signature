package com.demo.electronicsignature.domain

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainScreenViewModel : ViewModel() {


	private val _pdfFile : MutableLiveData<Uri> = MutableLiveData()
	val pdfFile : LiveData<Uri> = _pdfFile

	private val _appStatus : MutableLiveData<AppStatus> = MutableLiveData(AppStatus.EMPTY)
	val appStatus : LiveData<AppStatus> = _appStatus

	private val _signatureFile : MutableLiveData<Uri> = MutableLiveData()
	val signatureFile : LiveData<Uri> = _signatureFile

	private var imagePosition : Pair<Float, Float> = Pair(0f, 0f)

	fun registerPdf(uri : Uri) {
		_pdfFile.value = uri
		_appStatus.value = AppStatus.PDF_LOADED
	}

	fun selectSignatureFile(uri:Uri) {
		_appStatus.value = AppStatus.SIGNATURE_SELECTED
		_signatureFile.value = uri
	}

	fun registerImagePosition(x: Float, y: Float) {
		imagePosition = Pair(x, y)
	}

	fun signDocument(currentPage: Int) {

	}

	enum class AppStatus {
		EMPTY, PDF_LOADED, SIGNATURE_SELECTED, SIGNATURE_CREATED
	}
}