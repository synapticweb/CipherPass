package net.synapticweb.passman.entrydetail

import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.synapticweb.passman.R
import net.synapticweb.passman.databinding.CustomFieldDetailItemBinding
import net.synapticweb.passman.model.CustomField
import net.synapticweb.passman.entrydetail.CustomFieldsAdapter.ViewHolder

class CustomFieldsAdapter(private val viewModel: EntryDetailViewModel,
                          private val fragment: EntryDetailFragment
) :
    ListAdapter<CustomField, ViewHolder>(CustomFieldsCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item, fragment)
    }

    class ViewHolder private constructor(val binding : CustomFieldDetailItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: EntryDetailViewModel, item : CustomField, fragment: EntryDetailFragment) {
            binding.viewModel = viewModel
            binding.fragment = fragment
            binding.item = item

            binding.caption.text = item.fieldName
            binding.value.setText(if(item.value.isBlank())
                fragment.requireContext().resources.getString(R.string.value_not_set)
            else
                item.value
            )
        }

        companion object {
            fun from(parent : ViewGroup) : ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CustomFieldDetailItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class CustomFieldsCallback : DiffUtil.ItemCallback<CustomField>() {
    override fun areItemsTheSame(oldItem: CustomField, newItem: CustomField): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CustomField, newItem: CustomField): Boolean {
        return oldItem == newItem
    }
}