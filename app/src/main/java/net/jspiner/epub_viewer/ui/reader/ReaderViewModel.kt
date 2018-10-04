package net.jspiner.epub_viewer.ui.reader

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epub_viewer.ui.base.BaseViewModel
import net.jspiner.epubstream.EpubStream
import net.jspiner.epubstream.dto.ItemRef
import net.jspiner.epubstream.dto.NavPoint
import java.io.File

class ReaderViewModel : BaseViewModel() {

    val extractedEpub: Epub = Epub()

    private lateinit var file: File
    private lateinit var pageInfo: PageInfo
    private val navPointLocationMap: HashMap<String, ItemRef> = HashMap()

    private val spineSubject: BehaviorSubject<ItemRef> = BehaviorSubject.create()
    private val toolboxShowSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(true)
    private val pageSubject: BehaviorSubject<Int> = BehaviorSubject.create()
    private val pageDisplaySubject: BehaviorSubject<String> = BehaviorSubject.create()

    fun setEpubFile(file: File) {
        this.file = file
    }

    fun extractEpub(extractRootDirectory: File): Completable {
        val epubStream = EpubStream(file)
        return epubStream.unzip(extractRootDirectory.absolutePath)
            .toSingleDefault(0)
            .flatMap { epubStream.getExtractedDirectory() }.doOnSuccess { extractedEpub.extractedDirectory = it }
            .flatMap { epubStream.getMimeType() }.doOnSuccess { extractedEpub.mimeType = it }
            .flatMap { epubStream.getContainer() }.doOnSuccess { extractedEpub.container = it }
            .flatMap { epubStream.getOpf() }.doOnSuccess { extractedEpub.opf = it }
            .flatMap { epubStream.getToc() }.doOnSuccess { extractedEpub.toc = it }
            .doOnSuccess { calcNavPointInSpine() }
            .ignoreElement()
    }

    private fun calcNavPointInSpine() {
        fun findMatchItemInSpines(content: String): ItemRef {
            for (spineItem in extractedEpub.opf.spine.itemrefs) {
                val itemFile = toManifestItem(spineItem)
                val relativePath = itemFile.relativeTo(extractedEpub.extractedDirectory).path

                if (content == relativePath) return spineItem
            }
            throw RuntimeException("해당 navPoint 를 spine 에서 찾을 수 없음 content : $content")
        }

        for (navPoint in extractedEpub.toc.navMap.navPoints) {
            val navPointContent = navPoint.content.src.split("#")[0]

            navPointLocationMap[navPoint.id] = findMatchItemInSpines(navPointContent)
        }
    }

    fun getCurrentSpineItem(): Observable<ItemRef> = spineSubject

    fun navigateToPoint(navPoint: NavPoint) {
        val itemRef = navPointLocationMap[navPoint.id]

        if (itemRef != null) {
            spineSubject.onNext(itemRef)
        } else {
            throw RuntimeException("해당 navPoint 를 찾을 수 없음 id : $navPoint")
        }
    }

    fun toManifestItem(itemRef: ItemRef): File {
        val manifestItemList = extractedEpub.opf.manifest.items

        for (item in manifestItemList) {
            if (item.id == itemRef.idRef) {
                return extractedEpub.extractedDirectory.resolve(item.href)
            }
        }
        throw RuntimeException("해당 itemRef 를 manifest 에서 찾을 수 없음 id : $itemRef")
    }

    fun setToolboxVisible(isVisible: Boolean) = toolboxShowSubject.onNext(isVisible)

    fun getToolboxVisible() = toolboxShowSubject

    fun setPageInfo(pageInfo: PageInfo) {
        this.pageInfo = pageInfo
        pageSubject.onNext(0)
    }

    fun getPageInfo() = pageInfo

    fun getCurrentPage(): Observable<Int> = pageSubject

    fun getCurrentPageDisplay(): Observable<String> {
        return pageDisplaySubject
    }

    fun navigatePage(page: Int) {
        pageSubject.onNext(page)
        pageDisplaySubject.onNext("$page / ${getPageInfo().allPage}")
    }

    fun setPageWithoutNavigate(page: Int) {
        pageDisplaySubject.onNext("$page / ${getPageInfo().allPage}")
    }
}