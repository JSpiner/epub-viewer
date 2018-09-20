package net.jspiner.epub_viewer.ui.base

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import io.reactivex.subjects.CompletableSubject
import net.jspiner.epub_viewer.util.LifecycleTransformer
import net.jspiner.epub_viewer.util.initLazy

abstract class BaseActivity<Binding : ViewDataBinding> : AppCompatActivity() {

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun createViewModel(): BaseViewModel
    abstract fun loadState(bundle: Bundle)
    abstract fun saveState(bundle: Bundle)

    protected val binding: Binding by lazy { DataBindingUtil.setContentView(this, getLayoutId()) as Binding }
    protected val viewModel: BaseViewModel by lazy { createViewModel() }

    private val lifecycleSubject: CompletableSubject by lazy { CompletableSubject.create() }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.initLazy()
        viewModel.initLazy()

        when {
            savedInstanceState != null -> loadState(savedInstanceState)
            intent.extras != null -> loadState(intent.extras!!)
            else -> loadState(Bundle.EMPTY)
        }
    }

    protected fun <T> bindLifecycle(): LifecycleTransformer<T> {
        return LifecycleTransformer(lifecycleSubject)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleSubject.onComplete()
        viewModel.onDestroy()
    }

    final override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }
}