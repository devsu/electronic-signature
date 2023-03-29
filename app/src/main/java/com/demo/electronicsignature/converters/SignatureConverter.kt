package com.demo.electronicsignature.converters

import android.net.Uri
import com.demo.electronicsignature.data.database.model.SignatureFile
import com.demo.electronicsignature.domain.model.SignatureData
import java.io.File

fun SignatureFile.toSignatureData() = SignatureData(
		id = id,
		fileName = File(path).name,
		uploadedDate = registrationDate,
		uri = Uri.parse(path)
)

fun SignatureData.toSignatureFile() = SignatureFile(
		id = id,
		path = uri.toString(),
		registrationDate = uploadedDate
)