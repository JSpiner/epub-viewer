package net.jspiner.epub_viewer.ui.search

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import io.reactivex.android.schedulers.AndroidSchedulers
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ActivitySearchBinding
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epub_viewer.ui.base.BaseActivity
import net.jspiner.epub_viewer.ui.search.finder.ScrollPageFinder

class SearchActivity: BaseActivity<ActivitySearchBinding, SearchViewModel>() {

    companion object {
        val EXTRA_CIRCULAR_REVEAL_X = "extraX"
        val EXTRA_CIRCULAR_REVEAL_Y = "extraY"
        val EXTRA_EPUB = "extraEpub"
        val EXTRA_PAGE_INFO = "extraPageInfo"

        fun startActivity(activity: Activity, view: View, epub: Epub, pageInfo: PageInfo) {
            val revealX = (view.x + view.width / 2).toInt()
            val revealY = (view.y + view.height / 2).toInt()

            val intent = Intent(activity, SearchActivity::class.java)
            intent.putExtra(SearchActivity.EXTRA_CIRCULAR_REVEAL_X, revealX)
            intent.putExtra(SearchActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY)
            intent.putExtra(SearchActivity.EXTRA_EPUB, epub)
            intent.putExtra(SearchActivity.EXTRA_PAGE_INFO, pageInfo)

            ActivityCompat.startActivity(activity, intent, Bundle.EMPTY)
            activity.overridePendingTransition(0, 0)
        }
    }

    private val TRANSITION_TIME = 500L
    private var revealX: Int = 0
    private var revealY: Int = 0
    private lateinit var epub: Epub
    private lateinit var pageInfo: PageInfo
    private val searchAdapter = SearchAdapter()

    override fun getLayoutId() = R.layout.activity_search

    override fun createViewModel() = SearchViewModel()

    override fun loadState(bundle: Bundle) {
        revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0)
        revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0)
        epub = intent.getSerializableExtra(EXTRA_EPUB) as Epub
        pageInfo = intent.getSerializableExtra(EXTRA_PAGE_INFO) as PageInfo
        viewModel.setEpub(epub)
        viewModel.setPageFinder(ScrollPageFinder(this, epub, pageInfo))
    }

    override fun saveState(bundle: Bundle) {
        bundle.putInt(EXTRA_CIRCULAR_REVEAL_X, revealX)
        bundle.putInt(EXTRA_CIRCULAR_REVEAL_Y, revealY)
        bundle.putSerializable(EXTRA_EPUB, epub)
        bundle.putSerializable(EXTRA_PAGE_INFO, pageInfo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setNavigationBarColor(R.color.colorPrimaryDark)

        init()
    }

    private fun init() {
        initAnimation()
        binding.searchText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                //no-op
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //no-op
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.onTextChanged(s)
            }
        })
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = searchAdapter

        subscribe()
    }

    private fun initAnimation() {
        binding.root.visibility = View.INVISIBLE
        val viewTreeObserver = binding.root.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    revealActivity(revealX, revealY)
                    binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    private fun revealActivity(x: Int, y: Int) {
        val finalRadius = (Math.max(binding.root.width, binding.root.height) * 1.1).toFloat()
        val circularReveal = ViewAnimationUtils.createCircularReveal(binding.root, x, y, 0f, finalRadius)
        circularReveal.duration = TRANSITION_TIME
        circularReveal.interpolator = AccelerateInterpolator()

        binding.root.visibility = View.VISIBLE
        circularReveal.start()
    }

    private fun unRevealActivity() {
        val finalRadius = (Math.max(binding.root.width, binding.root.height) * 1.1).toFloat()
        val circularReveal = ViewAnimationUtils.createCircularReveal(
            binding.root, revealX, revealY, finalRadius, 0f
        )
        circularReveal.duration = TRANSITION_TIME
        circularReveal.interpolator = FastOutSlowInInterpolator()
        circularReveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.root.visibility = View.INVISIBLE
                finish()
                overridePendingTransition(0, 0)
            }
        })


        circularReveal.start()
    }

    override fun onBackPressed() {
        unRevealActivity()
    }

    private fun subscribe() {
        viewModel.getSearchResult()
            .compose(bindLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { searchAdapter.addData(it) }

        viewModel.getSearchReset()
            .compose(bindLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { searchAdapter.resetAll() }
    }
}