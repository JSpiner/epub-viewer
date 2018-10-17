package net.jspiner.epub_viewer.ui.base

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import io.reactivex.subjects.CompletableSubject
import net.jspiner.epub_viewer.util.LifecycleTransformer
import net.jspiner.epub_viewer.util.initLazy

abstract class BaseView<Binding : ViewDataBinding, ViewModel : BaseViewModel> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val lifecycleSubject: CompletableSubject by lazy { CompletableSubject.create() }

    @LayoutRes
    abstract fun getLayoutId(): Int

    protected val binding: Binding by lazy {
        DataBindingUtil.inflate(
            LayoutInflater.from(context),
            getLayoutId(),
            this,
            true
        ) as Binding
    }
    protected val viewModel: ViewModel by lazy { getActivity().viewModel }

    protected fun getActivity(): BaseActivity<*, ViewModel> {
        return context as BaseActivity<*, ViewModel>
    }

    init {
        binding.initLazy()
        viewModel.initLazy()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleSubject.onComplete()
    }

    protected fun <T> bindLifecycle(): LifecycleTransformer<T> {
        return LifecycleTransformer(lifecycleSubject)
    }
}