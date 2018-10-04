package net.jspiner.epub_viewer.ui.base

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.ColorRes
import android.support.annotation.LayoutRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import io.reactivex.subjects.CompletableSubject
import net.jspiner.epub_viewer.ui.common.LoadingDialog
import net.jspiner.epub_viewer.util.LifecycleTransformer
import net.jspiner.epub_viewer.util.initLazy

abstract class BaseActivity<Binding : ViewDataBinding, ViewModel : BaseViewModel> : AppCompatActivity() {

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun createViewModel(): ViewModel
    abstract fun loadState(bundle: Bundle)
    abstract fun saveState(bundle: Bundle)

    val binding: Binding by lazy { DataBindingUtil.setContentView(this, getLayoutId()) as Binding }
    val viewModel: ViewModel by lazy { createViewModel() }

    private val lifecycleSubject: CompletableSubject by lazy { CompletableSubject.create() }

    private val loadingDialog by lazy { LoadingDialog(this) }

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

    fun showStatusBar() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun hideStatusBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun showNavigationBar() {
        window.decorView.apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    fun hideNavigationBar() {
        window.decorView.apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    fun getNavigationBarHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    fun setNavigationBarColor(@ColorRes colorRes: Int) {
        window.navigationBarColor = ContextCompat.getColor(this, colorRes)
    }

    fun showLoading() = loadingDialog.show()

    fun hideLoading() = loadingDialog.hide()

}