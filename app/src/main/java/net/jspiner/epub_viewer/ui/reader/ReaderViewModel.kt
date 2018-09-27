package net.jspiner.epub_viewer.ui.reader

import io.reactivex.Completable
import net.jspiner.epub_viewer.ui.base.BaseViewModel
import net.jspiner.epubstream.EpubStream
import java.io.File

class ReaderViewModel : BaseViewModel() {

    private lateinit var file: File
    lateinit var epubStream: EpubStream

    fun setEpubFile(file: File) {
        this.file = file
        this.epubStream = EpubStream(file)
    }

    fun extractEpub(extractRootDirectory: File): Completable {
        return epubStream.unzip(extractRootDirectory.absolutePath)
            .toSingle { epubStream.getMimeType() }
            .flatMap { epubStream.getContainer() }
            .flatMap { epubStream.getOpf() }
            .flatMap { epubStream.getToc() }
            .ignoreElement()
    }
}