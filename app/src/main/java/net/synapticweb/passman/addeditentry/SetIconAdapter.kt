package net.synapticweb.passman.addeditentry

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.synapticweb.passman.R
import net.synapticweb.passman.databinding.IconItemBinding

class SetIconAdapter(private val viewModel : SetIconViewModel, private val context: Context) :
    RecyclerView.Adapter<SetIconAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return context.resources.getIntArray(R.array.icons).size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val typedArray = context.resources.obtainTypedArray(R.array.icons)
        val iconRes = typedArray.getResourceId(position, 0)
        typedArray.recycle()
        holder.bind(viewModel, iconRes)
    }

    class ViewHolder private constructor(val binding : IconItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel : SetIconViewModel, iconRes : Int) {
            binding.viewModel = viewModel
            binding.iconRes = iconRes
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