package net.synapticweb.cipherpass.addeditentry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.synapticweb.cipherpass.model.CustomField
import net.synapticweb.cipherpass.addeditentry.CustomFieldsAdapter.ViewHolder
import net.synapticweb.cipherpass.databinding.CustomFieldAddeditItemBinding


class CustomFieldsAdapter(private val fragment : CustomFieldsEditFragment) :
    ListAdapter<CustomField, ViewHolder>(CustomFieldsCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, fragment, position)
    }

    class ViewHolder private constructor(val binding : CustomFieldAddeditItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item : CustomField, fragment : CustomFieldsEditFragment, position: Int) {
            binding.item = item
            binding.fieldLayout.hint = item.fieldName
            binding.field.setText(item.value)

            binding.field.setOnFocusChangeListener { _, hasFocus ->
                    binding.field.tag = if(hasFocus) "got-focus" else null
            }

            binding.field.addTextChangedListener(
                afterTextChanged = { editable ->
                    if(binding.field.tag == "got-focus")
                        fragment.saveField(position, editable.toString())
                })

            binding.deleteField.setOnClickListener {
                fragment.manageDeletion(position)
            }

            binding.editField.setOnClickListener {
                fragment.manageEdit(position, item)
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
    fun saveField(position: Int, value : String)
    fun manageDeletion(position: Int)
    fun manageEdit(position: Int, item: CustomField)
}


class CustomFieldsCallback : DiffUtil.ItemCallback<CustomField>() {
    override fun areItemsTheSame(oldItem: CustomField, newItem: CustomField): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CustomField, newItem: CustomField): Boolean {
        return oldItem == newItem
    }
}