package net.jspiner.viewer.ui.search

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import net.jspiner.viewer.R
import net.jspiner.viewer.dto.SearchResult

class SearchAdapter : RecyclerView.Adapter<SearchViewHolder>() {

    private val dataList: ArrayList<SearchResult> = ArrayList()

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): SearchViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        return SearchViewHolder(
            DataBindingUtil.inflate(
                inflater,
                R.layout.item_search,
                viewGroup,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.setData(dataList[position])
    }

    fun addData(searchResult: SearchResult) {
        dataList.add(searchResult)
        notifyItemInserted(dataList.size)
    }

    fun resetAll() {
        dataList.clear()
        notifyDataSetChanged()
    }
}