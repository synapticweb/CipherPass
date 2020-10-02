package net.synapticweb.passman.credentialslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.synapticweb.passman.databinding.CredentialItemBinding
import net.synapticweb.passman.model.Credential
import net.synapticweb.passman.credentialslist.CredentialsAdapter.ViewHolder

class CredentialsAdapter(private val viewModel : CredListViewModel) :
    ListAdapter<Credential, ViewHolder>(CredDiffCallback()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: CredentialItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: CredListViewModel, item : Credential) {
            binding.viewModel = viewModel
            binding.credential = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent : ViewGroup) : ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CredentialItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class CredDiffCallback : DiffUtil.ItemCallback<Credential>() {
    override fun areItemsTheSame(oldItem: Credential, newItem: Credential): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Credential, newItem: Credential): Boolean {
        return oldItem == newItem
    }
}