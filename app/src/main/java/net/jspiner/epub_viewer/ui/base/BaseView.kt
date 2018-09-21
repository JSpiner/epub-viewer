package net.jspiner.epub_viewer.ui.base

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import net.jspiner.epub_viewer.util.initLazy

abstract class BaseView<Binding : ViewDataBinding> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

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
    protected val viewModel: BaseViewModel by lazy { getActivity().viewModel }

    private fun getActivity(): BaseActivity<*> {
        return context as BaseActivity<*>
    }

    init {
        binding.initLazy()
        viewModel.initLazy()
    }
}