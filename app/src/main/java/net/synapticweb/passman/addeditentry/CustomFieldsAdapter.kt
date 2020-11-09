package net.synapticweb.passman.addeditentry

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.synapticweb.passman.databinding.CustomFieldItemBinding
import net.synapticweb.passman.model.CustomField
import net.synapticweb.passman.addeditentry.CustomFieldsAdapter.ViewHolder


class CustomFieldsAdapter(private val viewModel: AddeditEntryViewModel,
                          private val savedData : MutableMap<Long, String>) :
    ListAdapter<CustomField, ViewHolder>(CustomFieldsCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item, savedData)
    }

    class ViewHolder private constructor(val binding : CustomFieldItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: AddeditEntryViewModel, item : CustomField,
                 savedData: MutableMap<Long, String>) {
            binding.field.setOnFocusChangeListener {field, hasFocus ->
                if(!hasFocus)
                    savedData[item.id] = (field as EditText).text.toString()
            }

            binding.deleteField.setOnClickListener {
                if(savedData.containsKey(item.id))
                    savedData.remove(item.id)
                viewModel.deleteCustomField(item)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent : ViewGroup) : ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CustomFieldItemBinding.inflate(layoutInflater, parent, false)
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