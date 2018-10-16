package net.jspiner.epub_viewer.ui.base

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.subjects.CompletableSubject
import net.jspiner.epub_viewer.util.LifecycleTransformer

abstract class BaseFragment<Binding: ViewDataBinding, ViewModel: BaseViewModel>: Fragment() {

    @LayoutRes
    abstract fun getLayoutId(): Int

    protected lateinit var binding: Binding
    protected val viewModel: ViewModel by lazy { (activity as BaseActivity<*, ViewModel>).viewModel }

    private val lifecycleSubject: CompletableSubject by lazy { CompletableSubject.create() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            getLayoutId(),
            container,
            false
        ) as Binding
        return binding.root
    }

    protected fun isBindingInitialized() = ::binding.isInitialized

    override fun onDestroy() {
        super.onDestroy()
        lifecycleSubject.onComplete()
    }

    protected fun <T> bindLifecycle(): LifecycleTransformer<T> {
        return LifecycleTransformer(lifecycleSubject)
    }

}