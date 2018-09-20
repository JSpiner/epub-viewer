package net.jspiner.epub_viewer.ui.reader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ActivityReaderBinding
import net.jspiner.epub_viewer.ui.base.BaseActivity
import java.io.File

const val INTENT_KEY_FILE = "intentKeyFile"

fun startReaderActivity(context: Context, epubFile: File) {
    val intent = Intent(context, ReaderActivity::class.java)
    intent.putExtra(INTENT_KEY_FILE, epubFile)
    context.startActivity(intent)
}

class ReaderActivity : BaseActivity<ActivityReaderBinding>() {

    override fun getLayoutId() = R.layout.activity_reader
    override fun createViewModel() = ReaderViewModel()

    private lateinit var epubFile: File

    override fun loadState(bundle: Bundle) {
        epubFile = bundle.getSerializable(INTENT_KEY_FILE) as File
    }

    override fun saveState(bundle: Bundle) {
        bundle.putSerializable(INTENT_KEY_FILE, epubFile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {

    }
}