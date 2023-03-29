package com.demo.electronicsignature.domain

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.electronicsignature.App
import com.demo.electronicsignature.converters.getFormattedDate
import com.demo.electronicsignature.converters.getFormattedTime
import com.itextpdf.io.image.ImageData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.StampingProperties
import com.itextpdf.signatures.*
import com.itextpdf.signatures.PdfSigner.CryptoStandard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.glxn.qrgen.android.QRCode
import net.glxn.qrgen.core.image.ImageType
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*


private const val SIGNATURE_WIDTH = 200f

private const val SIGNATURE_HEIGHT = 100f

class MainScreenViewModel : ViewModel() {


	private val _pdfFile: MutableLiveData<Uri> = MutableLiveData()
	val pdfFile: LiveData<Uri> = _pdfFile

	private val _appStatus: MutableLiveData<AppStatus> = MutableLiveData(AppStatus.EMPTY)
	val appStatus: LiveData<AppStatus> = _appStatus

	private val _signatureFile: MutableLiveData<Uri> = MutableLiveData()
	val signatureFile: LiveData<Uri> = _signatureFile

	private var _imagePosition: MutableLiveData<Pair<Float, Float>> = MutableLiveData()
	var imagePosition: LiveData<Pair<Float, Float>> = _imagePosition

	private var _shareDocument: MutableLiveData<String> = MutableLiveData()
	var shareDocument: LiveData<String> = _shareDocument

	private var _errorMessage: MutableLiveData<String> = MutableLiveData()
	var errorMessage: LiveData<String> = _errorMessage

	private var dimensions = Pair(0f, 0f)

	fun registerPdf(uri: Uri) {
		_pdfFile.value = uri
		_appStatus.value = AppStatus.PDF_LOADED
	}

	fun selectSignatureFile(uri: Uri) {
		_appStatus.value = AppStatus.SIGNATURE_SELECTED
		_signatureFile.value = uri
	}

	fun registerImagePosition(x: Float, y: Float) {
		_imagePosition.value = Pair(x, y)
	}

	fun signDocument(password: String, currentPage: Int) {
		try {
			_appStatus.value = AppStatus.PROCESSING_SIGNATURE

			val signature = getSignatureFile()

			val date = Date()

			val destName = getDestinyFileName(date)

			val destiny = App.context.filesDir.path + "/" + destName

			val pass = password.toCharArray()
			val provider = BouncyCastleProvider()
			Security.removeProvider("BC")
			Security.addProvider(provider)
			val ks = KeyStore.getInstance("pkcs12", provider.name)
			ks.load(signature.inputStream(), pass)
			val alias = ks.aliases().nextElement()
			val pk = ks.getKey(alias, pass) as PrivateKey
			val chain = ks.getCertificateChain(alias)
			viewModelScope.launch(Dispatchers.IO) {
				sign(
					pdfFile.value!!.path!!.split(":").last(),
					destiny,
					chain,
					pk,
					"SHA-256",
					provider.name,
					CryptoStandard.CMS,
					"SIGNATURE MAKE BY DEVSU",
					null,
					null,
					null,
					0,
					currentPage + 1
				)
				delay(1000)
				_appStatus.postValue(AppStatus.SIGNATURE_CREATED)
				_shareDocument.postValue(destiny)
			}
		}catch (e: NullPointerException){
			Log.e("NullPointerException", "Error: ${e.message}")
			_errorMessage.value = e.message
			_appStatus.value = AppStatus.ERROR
		}
		catch (e: SecurityException){
			Log.e("SecurityException", "Error: ${e.message}")
			_errorMessage.value = e.message
			_appStatus.value = AppStatus.ERROR
		}catch (e: KeyStoreException){
			Log.e("KeyStoreException", "Error: ${e.message}")
			_errorMessage.value = e.message
			_appStatus.value = AppStatus.ERROR
		}catch (e: NoSuchProviderException){
			Log.e("NoSuchProviderException", "Error: ${e.message}")
			_errorMessage.value = e.message
			_appStatus.value = AppStatus.ERROR
		}catch (e: CertificateException){
			Log.e("CertificateException", "Error: ${e.message}")
			_errorMessage.value = e.message
			_appStatus.value = AppStatus.ERROR
		}catch (e: IOException) {
			Log.e("IOException", "Error: ${e.message}")
			_errorMessage.value = e.message
			_appStatus.value = AppStatus.ERROR
		}catch (e: NoSuchAlgorithmException){
			Log.e("NoSuchAlgorithmException", "Error: ${e.message}")
			_errorMessage.value = e.message
			_appStatus.value = AppStatus.ERROR
		}catch (e: NoSuchElementException){
			Log.e("NoSuchElementException", "Error: ${e.message}")
			_errorMessage.value = e.message
			_appStatus.value = AppStatus.ERROR
		}catch (e: UnrecoverableKeyException){
			Log.e("UnrecoverableKeyException", "Error: ${e.message}")
			_errorMessage.value = e.message
			_appStatus.value = AppStatus.ERROR
		}catch (e: GeneralSecurityException){
			Log.e("GeneralSecurityException", "Error while signing PDF: ${e.message}")
			_errorMessage.value = e.message
			_appStatus.value = AppStatus.ERROR
		}


	}

	private fun getSignatureFile(): File {
		return try {
			File(signatureFile.value!!.path!!)
		} catch (e: NullPointerException) {
			Log.e("SIGN", "Error while get signature file: ${e.message}", e)
			throw e
		}
	}

	private fun getDestinyFileName(signDate: Date): String {
		return try {
			val pdf = File(pdfFile.value!!.path!!)
			"${
				pdf.name.split(".").first()
			} - Signed ${signDate.getFormattedDate("_")} at ${signDate.getFormattedTime("_")}.pdf"
		} catch (e: Exception) {
			Log.e("SIGN", "Error while create custom file name: ${e.message}", e)
			"${UUID.randomUUID()}.pdf"
		}
	}

	@Throws(GeneralSecurityException::class, IOException::class)
	fun sign(
		src: String?, dest: String?, chain: Array<Certificate?>?, pk: PrivateKey?,
		digestAlgorithm: String?, provider: String?, subfilter: CryptoStandard?,
		reason: String?, crlList: Collection<ICrlClient?>?,
		ocspClient: IOcspClient?, tsaClient: ITSAClient?, estimatedSize: Int, page: Int
	) {
		val reader = PdfReader(src)
		val signer = PdfSigner(reader, FileOutputStream(dest), StampingProperties())

		val (_, height) = dimensions

		val rect = Rectangle(
			imagePosition.value!!.first,
			height - imagePosition.value!!.second,
			SIGNATURE_WIDTH,
			SIGNATURE_HEIGHT
		)
		val appearance = signer.signatureAppearance
		appearance
			.setReason(reason)
			.setReuseAppearance(false)
			.setPageRect(rect)
			.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION)
			.pageNumber = page
		val image = createImage(chain?.get(0) as X509Certificate)
		appearance.signatureGraphic = image


		signer.fieldName = "sig"
		val pks: IExternalSignature = PrivateKeySignature(pk, digestAlgorithm, provider)
		val digest: IExternalDigest = BouncyCastleDigest()

		signer.signDetached(
			digest,
			pks,
			chain,
			crlList,
			ocspClient,
			tsaClient,
			estimatedSize,
			subfilter
		)
	}

	private fun createImage(chain: X509Certificate): ImageData {
		var text = ""
		val data = CertificateInfo.getSubjectFields(chain)
		data.fields.forEach {
			text += "${it.key}: ${it.value}\n"
		}
		CertificateInfo.getIssuerFields(chain).fields.forEach {
			text += "${it.key}: ${it.value}\n"
		}
		Log.i("SIGN", "text: $text")
		QRCode.from(text)
			.withSize(50, 50)
			.to(ImageType.PNG)
			.stream()
			.use { inputStream ->
				return ImageDataFactory.create(inputStream.toByteArray())
			}
	}

	fun setPageDimensions(pageWidth: Float, pageHeight: Float) {
		dimensions = Pair(pageWidth, pageHeight)
	}

	enum class AppStatus {
		EMPTY, PDF_LOADED, SIGNATURE_SELECTED, PROCESSING_SIGNATURE, SIGNATURE_CREATED, ERROR
	}
}