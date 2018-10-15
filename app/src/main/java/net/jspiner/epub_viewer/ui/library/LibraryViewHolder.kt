package net.jspiner.epub_viewer.ui.library

import android.support.v7.widget.RecyclerView
import net.jspiner.epub_viewer.databinding.ItemLibraryBinding

class LibraryViewHolder(val binding: ItemLibraryBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setData(epubPath: String) {
        binding.title.text = epubPath
    }

}