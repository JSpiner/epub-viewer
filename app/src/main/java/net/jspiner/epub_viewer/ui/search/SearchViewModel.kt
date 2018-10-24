package net.jspiner.epub_viewer.ui.search

import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.ui.base.BaseViewModel
import net.jspiner.epub_viewer.ui.search.finder.PageFinder
import net.jspiner.epubstream.dto.ItemRef
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.concurrent.TimeUnit

class SearchViewModel : BaseViewModel() {

    private val textSubject = PublishSubject.create<CharSequence>()
    private lateinit var epub: Epub
    private lateinit var pageFinder: PageFinder
    private var lastRequestDisposable: Disposable? = null

    init {
        textSubject.debounce(400, TimeUnit.MILLISECONDS)
            .compose(bindLifecycle())
            .subscribe { onTextChangedInternal(it.toString()) }
    }

    fun setEpub(epub: Epub) {
        this.epub = epub
    }

    fun setPageFinder(pageFinder: PageFinder) {
        this.pageFinder = pageFinder
    }

    fun onTextChanged(charSequence: CharSequence) {
        textSubject.onNext(charSequence)
    }

    private fun onTextChangedInternal(content: String) {
        lastRequestDisposable?.dispose()
        var startTime = System.currentTimeMillis()
        Observable.fromIterable(epub.opf.spine.itemrefs.toList())
            .map { toManifestItemPair(it) }
            .map { readFile(it) }
            .flatMap { findTextIndex(it, content) }
            .toFlowable(BackpressureStrategy.BUFFER)
            .flatMapSingle({ pageFinder.findPage(it.itemRef, it.index) }, false, 1)
            .subscribeOn(Schedulers.computation())
            .subscribe { println("page : " + it +" diff : " + (System.currentTimeMillis() - startTime)) }
            .also { lastRequestDisposable = it }
    }

    private fun toManifestItemPair(itemRef: ItemRef): ItemFile {
        val manifestItemList = epub.opf.manifest.items

        for (item in manifestItemList) {
            if (item.id == itemRef.idRef) {
                return ItemFile(itemRef, epub.extractedDirectory.resolve(item.href))
            }
        }
        throw RuntimeException("해당 itemRef 를 manifest 에서 찾을 수 없음 id : ${itemRef.idRef}")
    }

    private fun readFile(itemFile: ItemFile): ItemContent {
        return ItemContent(
            itemFile.itemRef,
            BufferedReader(FileReader(itemFile.file)).use { br ->
                val sb = StringBuilder()
                var line = br.readLine()

                while (line != null) {
                    sb.append(line)
                    sb.append(System.lineSeparator())
                    line = br.readLine()
                }
                sb.toString()
            }
        )
    }

    private fun findTextIndex(itemContent: ItemContent, content: String): Observable<ItemIndex> {
        return Observable.create { emitter ->
            val text = itemContent.content
            var isInHtmlTag = false
            val tempBuffer = StringBuilder()
            for (i in 0 until text.length) {
                if (text[i] == '<') isInHtmlTag = true
                if (isInHtmlTag && text[i] == '>') isInHtmlTag = false
                if (isInHtmlTag) continue

                tempBuffer.append(text[i])

                if (!content.startsWith(tempBuffer)) {
                    tempBuffer.delete(0, tempBuffer.length)
                } else if (content == tempBuffer.toString()) {
                    emitter.onNext(
                        ItemIndex(
                            itemContent.itemRef,
                            i
                        )
                    )
                    tempBuffer.delete(0, tempBuffer.length)
                }
            }
            emitter.onComplete()
        }
    }

    data class ItemFile(val itemRef: ItemRef, val file: File)
    data class ItemContent(val itemRef: ItemRef, val content: String)
    data class ItemIndex(val itemRef: ItemRef, val index:Int)

}