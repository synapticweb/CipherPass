package net.synapticweb.passman.entrydetail

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.synapticweb.passman.R
import net.synapticweb.passman.databinding.CustomFieldDetailItemBinding
import net.synapticweb.passman.model.CustomField
import net.synapticweb.passman.entrydetail.CustomFieldsAdapter.ViewHolder

class CustomFieldsAdapter(private val viewModel: EntryDetailViewModel,
                          private val context : Context
) :
    ListAdapter<CustomField, ViewHolder>(CustomFieldsCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item, context)
    }

    class ViewHolder private constructor(val binding : CustomFieldDetailItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: EntryDetailViewModel, item : CustomField, context: Context) {
            binding.viewModel = viewModel
            binding.caption.text = item.fieldName
            binding.value.text = if(item.value.isBlank())
                context.resources.getString(R.string.value_not_set)
            else
                item.value
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