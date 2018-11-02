package net.jspiner.viewer.ui.etc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import net.jspiner.viewer.R
import net.jspiner.viewer.databinding.ActivityEtcBinding
import net.jspiner.viewer.ui.base.BaseActivity
import java.io.File

class EtcActivity : BaseActivity<ActivityEtcBinding, EtcViewModel>() {

    companion object {
        val REQUEST_CODE = 1111
        val INTENT_KEY_TITLE = "intentKeyTitle"
        val INTENT_KEY_AUTHOR = "intentKeyAuthor"
        val INTENT_KEY_IMAGE_FILE = "intentKeyImageFile"

        val IS_SCROLL_MODE = "keyIsScrollMode"

        fun startActivityForResult(activity: Activity, title: String, author: String, imageFile: File?) {
            val intent = Intent(activity, EtcActivity::class.java)
            intent.putExtra(INTENT_KEY_TITLE, title)
            intent.putExtra(INTENT_KEY_AUTHOR, author)
            intent.putExtra(INTENT_KEY_IMAGE_FILE, imageFile)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }
    }

    private lateinit var title: String
    private lateinit var author: String
    private var imageFile: File? = null

    override fun getLayoutId() = R.layout.activity_etc

    override fun createViewModel() = EtcViewModel()

    override fun loadState(bundle: Bundle) {
        title = bundle.getString(INTENT_KEY_TITLE)!!
        author = bundle.getString(INTENT_KEY_AUTHOR)!!
        imageFile = bundle.getSerializable(INTENT_KEY_IMAGE_FILE) as File?
    }

    override fun saveState(bundle: Bundle) {
        intent.putExtra(INTENT_KEY_TITLE, title)
        intent.putExtra(INTENT_KEY_AUTHOR, author)
        intent.putExtra(INTENT_KEY_IMAGE_FILE, imageFile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
    }

    private fun init() {
        binding.title.text = title
        binding.author.text = author
        Glide.with(this)
            .load(imageFile)
            .into(binding.bookCover)

        binding.typeVertical!!.root.setOnClickListener { onTypeChangeClicked() }
        binding.typeHorizontal!!.root.setOnClickListener { onTypeChangeClicked() }
        binding.backButton.setOnClickListener { finish() }
    }

    private fun onTypeChangeClicked() {
        binding.typeVertical!!.apply { selected = !(selected ?: true) }
        binding.typeHorizontal!!.apply { selected = !(selected ?: false) }
    }

    override fun finish() {
        val intent = Intent()
        intent.putExtra(IS_SCROLL_MODE, binding.typeVertical!!.selected)
        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }
}