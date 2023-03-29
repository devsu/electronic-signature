package com.demo.electronicsignature.view.mainScreen

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.demo.electronicsignature.R
import com.demo.electronicsignature.databinding.FragmentMainScreenBinding
import com.demo.electronicsignature.domain.MainScreenViewModel
import com.demo.electronicsignature.view.bottomSheet.BottomSheet
import com.squareup.picasso.Picasso
import java.io.File


private const val AUTHORITY = "com.demo.electronicsignature.view.mainScreen.fileprovider"

class MainScreenFragment : Fragment() {

	private lateinit var _binding: FragmentMainScreenBinding
	private val binding get() = _binding
	private val viewModel: MainScreenViewModel by viewModels()
	private var currentPage: Int = 0
	private val toasts = mutableListOf<Toast>()
	private lateinit var loadContent: ActivityResultLauncher<String>

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentMainScreenBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		configurePdfLoadButton()
		configureFAB()
		subscribeToViewModel()
		configureMenu()
	}

	private fun configureMenu() {
		binding.toolbar.setOnMenuItemClickListener { menuItem ->
			when (menuItem.itemId) {
				R.id.searchPage -> {
					configureSearchPage()
				}
//				R.id.reloadDocument -> {
//					configureReloadDocument()
//					true
//
//				}

				else -> false
			}
		}
	}

	private fun configureReloadDocument(): Boolean {

		binding.mainInstruction.setOnClickListener {
			loadContent.launch("application/pdf")
		}
		return true
	}

	private fun configureSearchPage(): Boolean {
		val editText = EditText(context)
		editText.inputType = InputType.TYPE_CLASS_NUMBER
		AlertDialog.Builder(context)
			.setTitle("Select page to go to")
			.setView(editText)
			.setPositiveButton(getString(R.string.positive_message)) { dialog, _ ->
				val page = editText.text.toString()
				val pageNumber = Integer.parseInt(page)
				if (pageNumber < 1) {
					Toast.makeText(context, "Page number must be greater than 0", Toast.LENGTH_SHORT).show()
					dialog.dismiss()
				}
				binding.pdfView.jumpTo(Integer.parseInt(page) - 1, true)
			}
			.setNegativeButton(getString(R.string.cancel_button)) { dialog, _ ->
				dialog.cancel()
			}
			.show()
		return true
	}

	private fun configureFAB() {
		binding.getSign.setOnClickListener {
			val bottomSheet = BottomSheet(viewModel)
			bottomSheet.show(childFragmentManager, bottomSheet.tag)
		}
		binding.signDocument.setOnClickListener {
			val editText = EditText(context)
			editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
			AlertDialog.Builder(context)
				.setTitle(getString(R.string.enter_password_title))
				.setView(editText)
				.setPositiveButton(getString(R.string.positive_message)) { _, _ ->
					val password = editText.text.toString()
					viewModel.signDocument(password, currentPage)
				}
				.setNegativeButton(getString(R.string.cancel_button)) { _, _ -> }
				.show()

		}
	}

	private fun subscribeToViewModel() {
		viewModel.pdfFile.observe(viewLifecycleOwner) {
			configurePdfView(it)
		}
		viewModel.signatureFile.observe(viewLifecycleOwner) {
			binding.getSign.visibility = View.GONE
			binding.signDocument.visibility = View.VISIBLE
			binding.signaturePlaceholder.visibility = View.VISIBLE
			Picasso.get().load(R.drawable.testqr).into(binding.signaturePlaceholder)
		}
		viewModel.appStatus.observe(viewLifecycleOwner) {
			when (it) {
				MainScreenViewModel.AppStatus.PROCESSING_SIGNATURE -> {
					binding.progressBar.visibility = View.VISIBLE
				}
				MainScreenViewModel.AppStatus.SIGNATURE_SELECTED -> {
					binding.signaturePlaceholder.visibility = View.VISIBLE
				}
				MainScreenViewModel.AppStatus.ERROR -> {
					binding.progressBar.visibility = View.GONE
					AlertDialog.Builder(context)
						.setTitle(getString(R.string.error_title))
						.setMessage(viewModel.errorMessage.value)
						.setPositiveButton(getString(R.string.positive_message)) { dialog, _ ->
							dialog.dismiss()
						}
						.show()
				}
				else -> {
					binding.progressBar.visibility = View.GONE
				}
			}
		}


		viewModel.imagePosition.observe(viewLifecycleOwner) {
			binding.signaturePlaceholder.x = it.first
			binding.signaturePlaceholder.y = it.second
		}
		viewModel.shareDocument.observe(viewLifecycleOwner) {
			val fileUri = FileProvider.getUriForFile(
				requireContext(),
				AUTHORITY,
				File(it)
			)
			if (fileUri == null) {
				Log.i("MainScreenFragment", "shareDocument: fileUri is null")
				Toast.makeText(context, "Error sharing document", Toast.LENGTH_SHORT).show()
			}
			val intent = Intent(Intent.ACTION_SEND)
			intent.type = "application/pdf"
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			intent.putExtra(Intent.EXTRA_STREAM, fileUri)
			intent.setDataAndType(fileUri, requireContext().contentResolver.getType(fileUri))
			startActivity(Intent.createChooser(intent, "Share PDF"))
		}
	}

	private fun configurePdfLoadButton() {
		val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
			if (uri == null || uri.path == null) return@registerForActivityResult
			binding.mainInstruction.visibility = View.GONE
			binding.pdfView.visibility = View.VISIBLE
			viewModel.registerPdf(uri)
			binding.getSign.visibility = View.VISIBLE
		}
		binding.mainInstruction.setOnClickListener {
			getContent.launch("application/pdf")
		}
	}

	private fun configurePdfView(uri: Uri) {
		binding.pdfView
			.fromUri(uri)
			.swipeHorizontal(true)
			.onDraw { _, pageWidth, pageHeight, _ ->
				viewModel.setPageDimensions(pageWidth, pageHeight)
			}
			.enableDoubletap(false)
			.onTap { event: MotionEvent ->
				if (viewModel.appStatus.value != MainScreenViewModel.AppStatus.SIGNATURE_SELECTED) {
					return@onTap false
				}
				viewModel.registerImagePosition(event.x, event.y)
				true
			}
			.onPageChange { page, pageCount ->
				toasts.forEach {
					it.cancel()
				}
				toasts.clear()
				currentPage = page
				val toast = Toast.makeText(context, "Page ${page + 1} of $pageCount", Toast.LENGTH_LONG)
				toasts.add(toast)
				toast.setGravity(Gravity.TOP, 0, resources.getDimensionPixelSize(R.dimen.toolbar_height))
				toast.show()
			}
			.load()
	}
}