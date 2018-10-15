package net.jspiner.epub_viewer.ui.library

import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.GridLayoutManager
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ActivityLibraryBinding
import net.jspiner.epub_viewer.ui.base.BaseActivity
import java.io.File

class LibraryActivity : BaseActivity<ActivityLibraryBinding, LibraryViewModel>() {

    private val adapter = LibraryAdapter()

    override fun getLayoutId(): Int {
        return R.layout.activity_library
    }

    override fun createViewModel(): LibraryViewModel {
        return LibraryViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun loadState(bundle: Bundle) {
        //no-op
    }

    override fun saveState(bundle: Bundle) {
        //no-op
    }

    private fun init() {
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@LibraryActivity.adapter
        }

        getEpubFilePathList()
            .observeOn(Schedulers.io())
            .subscribe { it -> adapter.setDataList(it) }
    }

    private fun getEpubFilePathList(): Single<ArrayList<String>> {
        val cursor: Cursor = contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            null,
            null,
            null,
            null
        ) ?: return Single.just(ArrayList())

        return Single.create {emitter ->
            val epubFileList = ArrayList<String>()
            if (cursor.moveToFirst()) {
                do {
                    val file = File(cursor.getString(1))
                    if (file.extension == "epub") epubFileList.add(file.path)
                } while (cursor.moveToNext())
            }
            cursor.close()

            emitter.onSuccess(epubFileList)
        }
    }
}
