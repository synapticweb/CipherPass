package net.synapticweb.cipherpass.entrieslist

import android.content.Context
import android.view.LayoutInflater
import android.widget.PopupWindow
import net.synapticweb.cipherpass.databinding.SearchPopupBinding
import net.synapticweb.cipherpass.model.Entry

//https://medium.com/@naingdroid/create-android-dropdown-menu-with-popupwindow-1062cfd5cf77
class SearchPopup(val context: Context, val viewModel: EntriesListViewModel) :
    PopupWindow(context) {
    private val adapter = SearchEntriesAdapter(viewModel)

    init {
        setup()
    }

    private fun setup() {
        val binding = SearchPopupBinding.inflate(LayoutInflater.from(context))
        binding.searchEntries.adapter = adapter
        binding.searchEntries.setHasFixedSize(true)
        val windowWidthPx = context.resources.displayMetrics.widthPixels
        width = (windowWidthPx * 0.75).toInt()
        contentView = binding.root
    }

    fun setList(entriesList : List<Entry>) {
        adapter.submitList(entriesList)
    }
}