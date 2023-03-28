package com.demo.electronicsignature.view.mainScreen

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.demo.electronicsignature.R
import com.demo.electronicsignature.databinding.FragmentMainScreenBinding
import com.demo.electronicsignature.domain.MainScreenViewModel
import com.demo.electronicsignature.view.bottomSheet.BottomSheet
import com.squareup.picasso.Picasso


class MainScreenFragment : Fragment() {

	private lateinit var _binding: FragmentMainScreenBinding
	private val binding get() = _binding
	private val viewModel: MainScreenViewModel by viewModels()
	private var currentPage: Int = 0


	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentMainScreenBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		configurePdfLoadButton()
		configureFAB()
		subscribeToViewModel()

	}

	private fun configureFAB() {
		binding.getSign.setOnClickListener {
			val bottomSheet = BottomSheet(viewModel)
			bottomSheet.show(childFragmentManager, bottomSheet.tag)
		}
		binding.signDocument.setOnClickListener{
			viewModel.signDocument(currentPage)
			Toast.makeText(context, getString(R.string.document_selected_toast_message), Toast.LENGTH_SHORT).show()
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
			.onTap { event: MotionEvent ->
				if (viewModel.appStatus.value != MainScreenViewModel.AppStatus.SIGNATURE_SELECTED) {
					return@onTap false
				}
				viewModel.registerImagePosition(event.x, event.y)
				binding.signaturePlaceholder.x = event.x
				binding.signaturePlaceholder.y = event.y
				true
			}
			.onPageChange { page, _ ->
				currentPage = page
			}
			.load()
	}
}