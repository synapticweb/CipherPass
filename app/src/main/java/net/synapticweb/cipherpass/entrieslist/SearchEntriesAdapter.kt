package net.synapticweb.cipherpass.entrieslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.synapticweb.cipherpass.databinding.SearchPopupItemBinding
import net.synapticweb.cipherpass.model.Entry

class SearchEntriesAdapter(private val viewModel: EntriesListViewModel) :
    ListAdapter<Entry, SearchEntriesAdapter.ViewHolder>(EntriesDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: SearchPopupItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: EntriesListViewModel, item : Entry) {
            binding.viewModel = viewModel
            binding.entry = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent : ViewGroup) : ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SearchPopupItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}