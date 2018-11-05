package net.jspiner.epub_viewer.ui.reader.viewer

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup

class EpubPagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    private val itemMap: HashMap<Int, WebContainerFragment> = HashMap()
    private var pageCount = 0

    fun getFragmentAt(position: Int): WebContainerFragment {
        return itemMap[position] ?: throw RuntimeException("position에 item이 존재하지 않습니다. position : $position")
    }

    override fun getItem(position: Int): Fragment {
        val fragment = WebContainerFragment.newInstance()
        if (itemMap[position] != null) {
            throw RuntimeException("position에 item이 이미 존재합니다. position : $position")
        }

        itemMap[position] = fragment
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        if (itemMap[position] == null) {
            throw RuntimeException("position에 item이 존재하지 않습니다. position : $position")
        }

        itemMap.remove(position)
    }

    override fun getCount(): Int {
        return pageCount
    }

    fun setAllPageCount(allPage: Int) {
        pageCount = allPage
        notifyDataSetChanged()
    }
}