package com.demo.electronicsignature.view.signatureList

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.demo.electronicsignature.databinding.AddSignatureItemBinding
import com.demo.electronicsignature.databinding.SignatureListItemBinding
import com.demo.electronicsignature.domain.model.NewSignature
import com.demo.electronicsignature.domain.model.Signature
import com.demo.electronicsignature.domain.model.SignatureData
import java.util.*

class SignatureListAdapter(
	private val signatures: MutableList<Signature>,
	private val getContent: ActivityResultLauncher<String>,
	private val listener: OnItemClickListener
) : RecyclerView.Adapter<SignatureListAdapter.SignatureViewHolder>() {

	private lateinit var addSignatureItemBinding: AddSignatureItemBinding
	private lateinit var signatureListItemBinding: SignatureListItemBinding
	var selectedFile: Uri = Uri.EMPTY

	enum class ViewType {
		NEW_SIGNATURE, SIGNATURE
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignatureViewHolder {
		return when (viewType) {
			ViewType.NEW_SIGNATURE.ordinal -> {
				addSignatureItemBinding =
					AddSignatureItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
				SignatureViewHolder(addSignatureItemBinding.root)
			}
			else -> {
				signatureListItemBinding =
					SignatureListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
				SignatureViewHolder(signatureListItemBinding.root)
			}
		}
	}

	fun addSignatures(newData: List<Signature>) {
		if (newData.isEmpty()) return
		signatures.clear()
		signatures.addAll(newData)
//		notifyItemRangeInserted(signatures.size, newData.size)
		notifyDataSetChanged()
	}

	override fun onBindViewHolder(holder: SignatureViewHolder, position: Int) {
		holder.bind(signatures[position])
	}

	override fun getItemViewType(position: Int): Int {
		return when (position) {
			0 -> ViewType.NEW_SIGNATURE.ordinal
			else -> ViewType.SIGNATURE.ordinal
		}
	}

	override fun getItemCount(): Int = signatures.size


	inner class SignatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		fun bind(signature: Signature) {
			if (signature is NewSignature) {
				addSignatureItemBinding.textView.text = itemView.context.getString(signature.placeholder)
				configureNewItemClickListeners()
				return
			}
			if (signature is SignatureData) {
				signatureListItemBinding.date.text = signature.uploadedDate.parseDate()
				signatureListItemBinding.name.text = signature.fileName
				configureSignatureFileClickListener(signature)
				return
			}
		}

		private fun configureSignatureFileClickListener(signature: SignatureData) {
			signatureListItemBinding.signatureListItem.setOnClickListener {
				listener.onItemClick(signature.uri)
			}
		}

		private fun configureNewItemClickListeners() {
			addSignatureItemBinding.textView.setOnClickListener {
				getContent.launch("*/*")
			}
		}
	}

	interface OnItemClickListener {
		fun onItemClick(uri: Uri)
	}

}

private fun Date.parseDate(): String {
	val calendar = Calendar.getInstance()
	calendar.time = this
	val day = calendar.get(Calendar.DAY_OF_MONTH)
	val month = calendar.get(Calendar.MONTH) + 1
	val year = calendar.get(Calendar.YEAR)
	return "$day/$month/$year"
}
