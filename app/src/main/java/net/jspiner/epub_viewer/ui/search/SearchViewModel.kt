package net.jspiner.epub_viewer.ui.search

import io.reactivex.subjects.PublishSubject
import net.jspiner.epub_viewer.ui.base.BaseViewModel
import java.util.concurrent.TimeUnit

class SearchViewModel: BaseViewModel() {

    private val textSubject = PublishSubject.create<CharSequence>()

    init {
        textSubject.debounce(400, TimeUnit.MILLISECONDS)
            .compose(bindLifecycle())
            .subscribe { onTextChangedInternal(it) }
    }

    fun onTextChanged(charSequence: CharSequence) {
        textSubject.onNext(charSequence)
    }

    private fun onTextChangedInternal(charSequence: CharSequence) {
        println("onTextChanged : $charSequence")
    }
}