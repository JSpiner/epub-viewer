package net.jspiner.epub_viewer.ui.common

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import net.jspiner.epub_viewer.R

class LoadingDialog(context: Context) : AlertDialog(context) {

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_loading)
    }
}