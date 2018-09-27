package net.jspiner.epub_viewer.ui.reader

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.ui.base.BaseViewModel
import net.jspiner.epubstream.EpubStream
import net.jspiner.epubstream.dto.ItemRef
import java.io.File

class ReaderViewModel : BaseViewModel() {

    private lateinit var file: File
    private val spineSubject: BehaviorSubject<ItemRef> = BehaviorSubject.create()
    private val extractedEpub: Epub = Epub()

    fun setEpubFile(file: File) {
        this.file = file
    }

    fun extractEpub(extractRootDirectory: File): Completable {
        val epubStream = EpubStream(file)
        return epubStream.unzip(extractRootDirectory.absolutePath)
            .toSingleDefault(0)
            .flatMap { epubStream.getExtractedDirectory() }.doOnSuccess { extractedEpub.extractedDirectory = it }
            .flatMap { epubStream.getMimeType() }.doOnSuccess { extractedEpub.mimeType = it }
            .flatMap { epubStream.getContainer() }.doOnSuccess { extractedEpub.container = it }
            .flatMap { epubStream.getOpf() }.doOnSuccess { extractedEpub.opf = it }
            .flatMap { epubStream.getToc() }.doOnSuccess { extractedEpub.toc = it }
            .ignoreElement()
    }

    fun getCurrentSpineItem(): Observable<ItemRef> {
        return spineSubject
    }

    fun toManifestItem(itemRef: ItemRef): File {
        val manifestItemList = extractedEpub.opf.manifest.items

        for (item in manifestItemList) {
            if (item.id == itemRef.idRef) {
                return extractedEpub.extractedDirectory.resolve(item.href)
            }
        }
        throw RuntimeException("해당 itemRef 를 manifest 에서 찾을 수 없음 id : $itemRef")
    }
}