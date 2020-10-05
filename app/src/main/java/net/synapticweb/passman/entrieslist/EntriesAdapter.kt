package net.synapticweb.passman.entrieslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.synapticweb.passman.databinding.EntryItemBinding
import net.synapticweb.passman.model.Entry
import net.synapticweb.passman.entrieslist.EntriesAdapter.ViewHolder

class EntriesAdapter(private val viewModel : EntriesListViewModel) :
    ListAdapter<Entry, ViewHolder>(EntriesDiffCallback()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: EntryItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: EntriesListViewModel, item : Entry) {
            binding.viewModel = viewModel
            binding.entry = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent : ViewGroup) : ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EntryItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class EntriesDiffCallback : DiffUtil.ItemCallback<Entry>() {
    override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Entry, newItem: Entry): Boolean {
        return oldItem == newItem
    }
}