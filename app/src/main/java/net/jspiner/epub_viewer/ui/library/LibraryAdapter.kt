package net.jspiner.epub_viewer.ui.library

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import net.jspiner.epub_viewer.R

class LibraryAdapter : RecyclerView.Adapter<LibraryViewHolder>() {

    private val dataList: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): LibraryViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        return LibraryViewHolder(
            DataBindingUtil.inflate(
                inflater,
                R.layout.item_library,
                viewGroup,
                false
            )
        )
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(viewHolder: LibraryViewHolder, position: Int) {
        viewHolder.setData(dataList[position])
    }

}