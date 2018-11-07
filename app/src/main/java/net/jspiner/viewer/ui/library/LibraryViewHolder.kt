package net.jspiner.viewer.ui.library

import android.support.v7.widget.RecyclerView
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.jspiner.viewer.databinding.ItemLibraryBinding
import net.jspiner.viewer.ui.reader.ReaderActivity
import net.jspiner.epubstream.EpubStream
import java.io.File

class LibraryViewHolder(val binding: ItemLibraryBinding) : RecyclerView.ViewHolder(binding.root) {

    private fun getContext() = binding.root.context!!
    private lateinit var epubPath: String

    init {
        binding.root.setOnClickListener {
            ReaderActivity.startActivity(getContext(), File(epubPath))
        }
    }

    fun setData(epubPath: String) {
        this.epubPath = epubPath

        val epubFile = File(epubPath)
        val epubStream = EpubStream(epubFile)
        epubStream.unzip(getContext().cacheDir.resolve(epubFile.name).absolutePath)
            .toSingleDefault(0)
            .flatMap { epubStream.getOpf() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { opf ->
                fun toManifestItem(id: String): File {
                    val manifestItemList = opf.manifest.items

                    for (item in manifestItemList) {
                        if (item.id == id) {
                            return epubStream.getExtractedDirectory().blockingGet().resolve(item.href)
                        }
                    }
                    throw RuntimeException("해당 itemRef 를 manifest 에서 찾을 수 없음 id : $id")
                }

                binding.title.text = opf.metaData.title

                val coverImage = opf.metaData.meta?.get("cover")
                if (coverImage != null) {
                    Glide.with(getContext())
                        .load(toManifestItem(coverImage))
                        .into(binding.bookCover)
                }
            }
    }
}