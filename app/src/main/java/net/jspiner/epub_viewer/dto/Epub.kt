package net.jspiner.epub_viewer.dto

import net.jspiner.epubstream.dto.Container
import net.jspiner.epubstream.dto.MimeType
import net.jspiner.epubstream.dto.Ncx
import net.jspiner.epubstream.dto.Package
import java.io.File
import java.io.Serializable

class Epub : Serializable {
    lateinit var extractedDirectory: File
    lateinit var mimeType: MimeType
    lateinit var container: Container
    lateinit var opf: Package
    lateinit var toc: Ncx
}