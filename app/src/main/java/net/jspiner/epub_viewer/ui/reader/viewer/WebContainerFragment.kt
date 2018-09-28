package net.jspiner.epub_viewer.ui.reader.viewer

import android.os.Bundle
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.FragmentWebContainerBinding
import net.jspiner.epub_viewer.ui.base.BaseFragment
import net.jspiner.epub_viewer.ui.base.EpubWebClient
import net.jspiner.epub_viewer.ui.reader.ReaderViewModel
import java.io.File

class WebContainerFragment: BaseFragment<FragmentWebContainerBinding, ReaderViewModel>() {

    companion object {
        fun newInstance(): WebContainerFragment {
            val fragment = WebContainerFragment()

            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_web_container

    override fun onStart() {
        super.onStart()

        binding.webView.webViewClient = EpubWebClient()
    }

    fun loadFile(file: File) {
        binding.webView.loadUrl(file.toURI().toURL().toString())
    }
}