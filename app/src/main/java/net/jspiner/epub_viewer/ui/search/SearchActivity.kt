package net.jspiner.epub_viewer.ui.search

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import net.jspiner.epub_viewer.R
import net.jspiner.epub_viewer.databinding.ActivitySearchBinding
import net.jspiner.epub_viewer.ui.base.BaseActivity

class SearchActivity: BaseActivity<ActivitySearchBinding, SearchViewModel>() {

    companion object {
        val EXTRA_CIRCULAR_REVEAL_X = "extraX"
        val EXTRA_CIRCULAR_REVEAL_Y = "extraY"

        fun startActivity(activity: Activity, view: View) {
            val revealX = (view.x + view.width / 2).toInt()
            val revealY = (view.y + view.height / 2).toInt()

            val intent = Intent(activity, SearchActivity::class.java)
            intent.putExtra(SearchActivity.EXTRA_CIRCULAR_REVEAL_X, revealX)
            intent.putExtra(SearchActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY)

            ActivityCompat.startActivity(activity, intent, Bundle.EMPTY)
            activity.overridePendingTransition(0, 0)
        }
    }

    private val TRANSITION_TIME = 500L
    private var revealX: Int = 0
    private var revealY: Int = 0

    override fun getLayoutId() = R.layout.activity_search

    override fun createViewModel() = SearchViewModel()

    override fun loadState(bundle: Bundle) {
        binding.root.visibility = View.INVISIBLE

        revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0)
        revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0)

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

    override fun saveState(bundle: Bundle) {
        bundle.putInt(EXTRA_CIRCULAR_REVEAL_X, revealX)
        bundle.putInt(EXTRA_CIRCULAR_REVEAL_Y, revealY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setNavigationBarColor(R.color.colorPrimaryDark)

        init()
    }

    private fun init() {
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
    }

    private fun revealActivity(x: Int, y: Int) {
        val finalRadius = (Math.max(binding.root.width, binding.root.height) * 1.1).toFloat()
        val circularReveal = ViewAnimationUtils.createCircularReveal(binding.root, x, y, 0f, finalRadius)
        circularReveal.duration = TRANSITION_TIME
        circularReveal.interpolator = FastOutSlowInInterpolator()

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
}