package net.jspiner.epub_viewer.ui.search

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.ui.base.BaseViewModel
import net.jspiner.epubstream.dto.ItemRef
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.concurrent.TimeUnit

class SearchViewModel : BaseViewModel() {

    private val textSubject = PublishSubject.create<CharSequence>()
    private lateinit var epub: Epub
    private var lastRequestDisposable: Disposable? = null

    init {
        textSubject.debounce(400, TimeUnit.MILLISECONDS)
            .compose(bindLifecycle())
            .subscribe { onTextChangedInternal(it) }
    }

    fun setEpub(epub: Epub) {
        this.epub = epub
    }

    fun onTextChanged(charSequence: CharSequence) {
        textSubject.onNext(charSequence)
    }

    private fun onTextChangedInternal(charSequence: CharSequence) {
        val startTime = System.currentTimeMillis()
        lastRequestDisposable?.dispose()
        Observable.fromIterable(epub.opf.spine.itemrefs.toList())
            .map { toManifestItemPair(it) }
            .map { readFile(it) }
            .flatMap { findTextIndex(it, charSequence) }
            .subscribeOn(Schedulers.io())
            .subscribe { println("index : $it"+ " end job + " + (System.currentTimeMillis() - startTime))}
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

    private fun findTextIndex(itemContent: ItemContent, charSequence: CharSequence): Observable<ItemIndex> {
        return Observable.create { emitter ->
            val text = itemContent.content
            var isInHtmlTag = false
            var tempBuffer = ""
            for (i in 0 until text.length) {
                if (text[i] == '<') isInHtmlTag = true
                if (isInHtmlTag && text[i] == '>') isInHtmlTag = false
                if (isInHtmlTag) continue

                tempBuffer += text[i]

                if (!charSequence.startsWith(tempBuffer)) {
                    tempBuffer = ""
                } else if (charSequence.toString() == tempBuffer) {
                    emitter.onNext(
                        ItemIndex(
                            itemContent.itemRef,
                            itemContent.content,
                            i
                        )
                    )
                }
            }
            emitter.onComplete()
        }
    }

    data class ItemFile(val itemRef: ItemRef, val file: File)
    data class ItemContent(val itemRef: ItemRef, val content: String)
    data class ItemIndex(val itemRef: ItemRef, val content: String, val index:Int)

}