package net.jspiner.epub_viewer.util

import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.CompletableTransformer
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.Maybe
import io.reactivex.MaybeSource
import io.reactivex.MaybeTransformer
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.SingleTransformer
import io.reactivex.subjects.CompletableSubject
import org.reactivestreams.Publisher

class LifecycleTransformer<T>(var lifecycleSubject: CompletableSubject) : ObservableTransformer<T, T>,
    FlowableTransformer<T, T>,
    SingleTransformer<T, T>,
    MaybeTransformer<T, T>,
    CompletableTransformer {

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream.takeUntil(lifecycleSubject.toObservable<T>())
    }

    override fun apply(upstream: Flowable<T>): Publisher<T> {
        return upstream.takeUntil(lifecycleSubject.toObservable<T>().toFlowable(BackpressureStrategy.BUFFER))
    }

    override fun apply(upstream: Single<T>): SingleSource<T> {
        return upstream.takeUntil(lifecycleSubject.toObservable<T>().singleOrError())
    }

    override fun apply(upstream: Maybe<T>): MaybeSource<T> {
        return upstream.takeUntil(lifecycleSubject.toObservable<T>().firstElement())
    }

    override fun apply(upstream: Completable): CompletableSource {
        return upstream.takeUntil(lifecycleSubject)
    }
}