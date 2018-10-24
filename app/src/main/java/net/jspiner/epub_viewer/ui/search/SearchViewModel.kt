package net.jspiner.epub_viewer.ui.search

import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.dto.SearchResult
import net.jspiner.epub_viewer.ui.base.BaseViewModel
import net.jspiner.epub_viewer.ui.search.finder.PageFinder
import net.jspiner.epubstream.dto.ItemRef
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.concurrent.TimeUnit

class SearchViewModel : BaseViewModel() {

    private val SEARCH_RESULT_RENDER_THRESHOLD = 100
    private val textSubject = PublishSubject.create<CharSequence>()
    private lateinit var epub: Epub
    private lateinit var pageFinder: PageFinder
    private var lastRequestDisposable: Disposable? = null
    private val searchItemSubject = PublishSubject.create<SearchResult>()
    private val searchResetSubject = PublishSubject.create<Boolean>()

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
        searchResetSubject.onNext(true)
        Observable.fromIterable(epub.opf.spine.itemrefs.toList())
            .map { toManifestItemPair(it) }
            .map { readFile(it) }
            .flatMap { findTextIndex(it, content) }
            .toFlowable(BackpressureStrategy.BUFFER)
            .flatMapSingle({ findPage(it) }, false, 1)
            .map { toSearchResult(it, content) }
            .subscribeOn(Schedulers.computation())
            .subscribe { searchItemSubject.onNext(it) }
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

    private fun findPage(itemIndex: ItemIndex): Single<PageIndex>? {
        return pageFinder.findPage(itemIndex.itemRef, itemIndex.index)
            .map { page -> PageIndex(page, itemIndex.itemRef, itemIndex.index) }
    }

    private fun toSearchResult(pageIndex: SearchViewModel.PageIndex, searchText: String): SearchResult {
        return SearchResult(
            toColoredSpannable(pageIndex, searchText),
            pageIndex.page
        )
    }

    private fun toColoredSpannable(pageIndex: SearchViewModel.PageIndex, searchText: String): Spannable {
        val fullContent = readFile(toManifestItemPair(pageIndex.itemRef)).content

        val splitStart = Math.max(0, pageIndex.index - SEARCH_RESULT_RENDER_THRESHOLD)
        val splitEnd = Math.min(pageIndex.index + SEARCH_RESULT_RENDER_THRESHOLD, fullContent.length)
        val innerIndex = pageIndex.index - splitStart

        val splittedContent = fullContent.substring(splitStart, splitEnd)

        val preContent = splittedContent.substring(0, innerIndex)
        val renderLengthDiff = preContent.length - fromHtml(preContent).toString().trimIndent().length

        val spannable = SpannableString(fromHtml(splittedContent))
        spannable.setSpan(
            ForegroundColorSpan(Color.BLUE),
            (innerIndex - renderLengthDiff) - searchText.length  + 1,
            (innerIndex - renderLengthDiff) + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun fromHtml(text: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(text)
        }
    }

    data class ItemFile(val itemRef: ItemRef, val file: File)
    data class ItemContent(val itemRef: ItemRef, val content: String)
    data class ItemIndex(val itemRef: ItemRef, val index:Int)
    data class PageIndex(val page: Int, val itemRef: ItemRef, val index: Int)

}