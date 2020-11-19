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


class CustomFieldsAdapter(private val fragment : CustomFieldsEditFragment) :
    ListAdapter<CustomField, ViewHolder>(CustomFieldsCallback()) {

    fun deleteItem(item: CustomField) {
        val list = currentList.toMutableList()
        list.remove(item)
        submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, fragment)
    }

    class ViewHolder private constructor(val binding : CustomFieldAddeditItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item : CustomField, fragment : CustomFieldsEditFragment) {
            binding.item = item
            binding.fieldLayout.hint = item.fieldName
            binding.field.setText(item.value)

            binding.field.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus)
                    binding.field.tag = "got-focus"
            }

            binding.field.addTextChangedListener(
                afterTextChanged = { editable ->
                    if(binding.field.tag == "got-focus")
                        fragment.saveField(item.id, editable.toString())
                })

            binding.deleteField.setOnClickListener {
                fragment.manageDeletion(item)
            }

            binding.editField.setOnClickListener {
                fragment.manageEdit(item)
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
    fun saveField(id : Long, value : String)
    fun manageDeletion(item : CustomField)
    fun manageEdit(item : CustomField)
}


class CustomFieldsCallback : DiffUtil.ItemCallback<CustomField>() {
    override fun areItemsTheSame(oldItem: CustomField, newItem: CustomField): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CustomField, newItem: CustomField): Boolean {
        return oldItem == newItem
    }
}