package net.jspiner.epub_viewer.ui.search

import android.support.v7.widget.RecyclerView
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.jspiner.epub_viewer.databinding.ItemLibraryBinding
import net.jspiner.epub_viewer.databinding.ItemSearchBinding
import net.jspiner.epub_viewer.dto.SearchResult
import net.jspiner.epub_viewer.ui.reader.startReaderActivity
import net.jspiner.epubstream.EpubStream
import java.io.File

class SearchViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {

    private fun getContext() = binding.root.context!!
    private lateinit var epubPath: String

    init {
        binding.root.setOnClickListener {
        }
    }

    fun setData(searchResult: SearchResult) {
        binding.text.text = searchResult.contentDisplay
    }
}