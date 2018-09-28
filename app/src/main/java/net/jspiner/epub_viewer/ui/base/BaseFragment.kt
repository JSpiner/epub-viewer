package net.jspiner.epub_viewer.ui.base

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class BaseFragment<Binding: ViewDataBinding, ViewModel: BaseViewModel>: Fragment() {

    @LayoutRes
    abstract fun getLayoutId(): Int

    protected lateinit var binding: Binding
    protected val viewModel: ViewModel by lazy { (activity as BaseActivity<*, ViewModel>).viewModel }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            getLayoutId(),
            container,
            false
        ) as Binding
        return binding.root
    }

}