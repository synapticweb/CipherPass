/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ). 
 * See the LICENSE file in the project root for license terms. 
 */

package net.synapticweb.cipherpass.entrydetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.databinding.CustomFieldDetailItemBinding
import net.synapticweb.cipherpass.model.CustomField
import net.synapticweb.cipherpass.entrydetail.CustomFieldsAdapter.ViewHolder

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
            binding.value.setText(item.value ?:
               fragment.requireContext().resources.getString(R.string.value_not_set))
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