/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.addeditentry

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.databinding.IconItemBinding

class SetIconAdapter(private val viewModel : SetIconViewModel,  val context: Context) :
    RecyclerView.Adapter<SetIconAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return context.resources.getIntArray(R.array.icons).size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val iconNames = context.resources.getStringArray(R.array.icons)
        val iconName = iconNames[position]
        val iconRes = context.resources.getIdentifier(iconName, "drawable", context.packageName)
        holder.bind(viewModel, iconName, iconRes)
    }

    class ViewHolder private constructor(val binding : IconItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel : SetIconViewModel, iconName : String, iconRes : Int) {
            binding.viewModel = viewModel
            binding.iconName = iconName
            binding.icon.setImageResource(iconRes)
        }

        companion object {
            fun from(parent : ViewGroup) : ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = IconItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}