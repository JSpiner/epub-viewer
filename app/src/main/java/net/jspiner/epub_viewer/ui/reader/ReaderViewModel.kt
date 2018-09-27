package net.jspiner.epub_viewer.ui.reader

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import net.jspiner.epub_viewer.ui.base.BaseViewModel
import net.jspiner.epubstream.EpubStream
import net.jspiner.epubstream.dto.ItemRef
import java.io.File

class ReaderViewModel : BaseViewModel() {

    private lateinit var file: File
    lateinit var epubStream: EpubStream
    private val spineSubject: BehaviorSubject<ItemRef> = BehaviorSubject.create()

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

    fun getCurrentSpineItem(): Observable<ItemRef> {
        return spineSubject
    }

    fun toManifestItem(itemRef: ItemRef): Single<File> {
        return epubStream.getOpf()
            .map { it.manifest.items.toList() }
            .flatMapObservable { Observable.fromIterable(it) }
            .filter { it.id == itemRef.idRef }
            .firstOrError()
            .map { it.href }
            .zipWith(
                epubStream.getExtractedDirectory(),
                BiFunction { href: String, file: File -> file.resolve(href) }
            )
    }
}