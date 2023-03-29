package com.demo.electronicsignature.view.bottomSheet

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.electronicsignature.R
import com.demo.electronicsignature.databinding.ModalBottomSheetContentBinding
import com.demo.electronicsignature.domain.MainScreenViewModel
import com.demo.electronicsignature.domain.SignatureListViewModel
import com.demo.electronicsignature.domain.model.SignatureData
import com.demo.electronicsignature.view.signatureList.SignatureListAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheet(
	private val viewModel: MainScreenViewModel
) : BottomSheetDialogFragment(), SignatureListAdapter.OnItemClickListener {

	private lateinit var _binding: ModalBottomSheetContentBinding
	private val binding get() = _binding
	private lateinit var adapter: SignatureListAdapter
	private val viewModels: SignatureListViewModel by viewModels()

	private val getContent: ActivityResultLauncher<String> = registerForActivityResult(
		ActivityResultContracts.GetContent()
	) { uri ->
		if (uri != null) {
			viewModels.addFile(uri)
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		// Inflate the layout for this fragment
		_binding = ModalBottomSheetContentBinding.inflate(inflater, container, false)
		return _binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		adapter = SignatureListAdapter(mutableListOf(), getContent, this)
		binding.signList.adapter = adapter
		binding.signList.layoutManager = LinearLayoutManager(context)
		configureObservers()
		viewModels.loadDatabase()
	}

	private fun configureObservers() {
		viewModels.filesList.observe(viewLifecycleOwner) {
			adapter.addSignatures(it)
		}
		viewModels.filesDeletedCount.observe(viewLifecycleOwner) {
			if (it > 0) {
				Toast.makeText(context, getString(R.string.file_deleted_toast_message, it), Toast.LENGTH_LONG).show()
			}
		}

		viewModels.fileDeletedId.observe(viewLifecycleOwner) {
			adapter.deleteSignature(it)
		}
	}

	override fun onItemClick(uri: Uri) {
		Log.i("BottomSheet", "onItemClick: $uri")
		viewModel.selectSignatureFile(uri)
		dismiss()
	}

	override fun onDeleteItem(signature: SignatureData) {
		AlertDialog.Builder(context)
			.setTitle(getString(R.string.delete_file_dialog_title))
			.setMessage(getString(R.string.delete_file_dialog_message))
			.setPositiveButton(getString(R.string.delete_file_dialog_positive_button)) { _, _ ->
				viewModels.deleteFile(signature)
			}
			.setNegativeButton(getString(R.string.cancel_button)) { _, _ ->
				dismiss()
			}
			.show()
	}
}