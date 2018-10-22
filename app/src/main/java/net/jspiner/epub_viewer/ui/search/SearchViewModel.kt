package net.jspiner.epub_viewer.ui.search

import io.reactivex.subjects.PublishSubject
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.ui.base.BaseViewModel
import java.util.concurrent.TimeUnit

class SearchViewModel : BaseViewModel() {

    private val textSubject = PublishSubject.create<CharSequence>()
    private lateinit var epub: Epub

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
        println("onTextChanged : $charSequence")
    }
}