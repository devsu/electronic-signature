package com.demo.electronicsignature.domain.model

import android.net.Uri
import androidx.annotation.StringRes
import java.util.Date

sealed class Signature
class SignatureData(val fileName: String, val uploadedDate: Date, val uri: Uri) : Signature()
class NewSignature(@StringRes val placeholder: Int) : Signature()