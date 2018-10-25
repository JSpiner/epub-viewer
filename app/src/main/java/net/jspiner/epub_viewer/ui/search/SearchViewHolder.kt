package net.jspiner.epub_viewer.ui.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
    private lateinit var searchResult: SearchResult

    init {
        binding.root.setOnClickListener {
            val intent = Intent()
            intent.putExtra(SearchActivity.EXTRA_SEARCH_RESULT_PAGE, searchResult.page)

            val activity = (getContext() as Activity)
            activity.setResult(Activity.RESULT_OK, intent)
            activity.finish()
        }
    }

    fun setData(searchResult: SearchResult) {
        this.searchResult = searchResult

        binding.text.text = searchResult.contentDisplay
        binding.page.text = "P. ${searchResult.page}"
    }
}