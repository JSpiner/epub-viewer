package net.jspiner.epub_viewer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import net.jspiner.epubstream.EpubStream
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var epubStream = EpubStream(File("test"))
    }
}
