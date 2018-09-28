package net.jspiner.epub_viewer.ui.reader.viewer

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import net.jspiner.epub_viewer.R

class EpubPagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return WebContainerFragment.newInstance()
    }

    override fun getCount(): Int {
        return 50
    }
}