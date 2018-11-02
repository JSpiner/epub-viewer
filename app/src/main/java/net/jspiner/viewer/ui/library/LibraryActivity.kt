package net.jspiner.viewer.ui.library

import android.Manifest
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.GridLayoutManager
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.jspiner.viewer.R
import net.jspiner.viewer.databinding.ActivityLibraryBinding
import net.jspiner.viewer.ui.base.BaseActivity
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
        // no-op
    }

    override fun saveState(bundle: Bundle) {
        // no-op
    }

    private fun init() {
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@LibraryActivity.adapter
        }
        requestPermission()
    }

    private fun requestPermission() {
        TedPermission.with(this)
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    loadEpub()
                }

                override fun onPermissionDenied(list: MutableList<String>?) {
                    requestPermission()
                }
            }).check()
    }

    private fun loadEpub() {
        getEpubFilePathList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindLifecycle())
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

        return Single.create { emitter ->
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
