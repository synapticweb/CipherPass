package net.synapticweb.passman.addeditentry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.synapticweb.passman.model.CustomField
import net.synapticweb.passman.addeditentry.CustomFieldsAdapter.ViewHolder
import net.synapticweb.passman.databinding.CustomFieldAddeditItemBinding


class CustomFieldsAdapter(private val viewModel: AddeditEntryViewModel,
                          private val fragment : CustomFieldsEditFragment) :
    ListAdapter<CustomField, ViewHolder>(CustomFieldsCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item, fragment)
    }

    class ViewHolder private constructor(val binding : CustomFieldAddeditItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: AddeditEntryViewModel, item : CustomField,
                 fragment : CustomFieldsEditFragment) {
            binding.fieldLayout.hint = item.fieldName
            binding.field.setText(item.value)

            binding.field.addTextChangedListener(
                afterTextChanged = { editable ->
                    fragment.saveCustomField(item.id, editable.toString())
                })

            binding.deleteField.setOnClickListener {
                fragment.removeCustomField(item.id)
                viewModel.deleteCustomField(item)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent : ViewGroup) : ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CustomFieldAddeditItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

interface CustomFieldsEditFragment {
    fun saveCustomField(id : Long, value : String)
    fun removeCustomField(id : Long)
}


class CustomFieldsCallback : DiffUtil.ItemCallback<CustomField>() {
    override fun areItemsTheSame(oldItem: CustomField, newItem: CustomField): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CustomField, newItem: CustomField): Boolean {
        return oldItem == newItem
    }
}