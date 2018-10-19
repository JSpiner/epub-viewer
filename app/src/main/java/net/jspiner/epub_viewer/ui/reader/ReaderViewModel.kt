package net.jspiner.epub_viewer.ui.reader

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import net.jspiner.epub_viewer.dto.Epub
import net.jspiner.epub_viewer.dto.PageInfo
import net.jspiner.epub_viewer.dto.ViewerType
import net.jspiner.epub_viewer.ui.base.BaseViewModel
import net.jspiner.epub_viewer.ui.reader.strategy.PageTypeStrategy
import net.jspiner.epub_viewer.ui.reader.strategy.ScrollTypeStrategy
import net.jspiner.epub_viewer.ui.reader.strategy.ViewerTypeStrategy
import net.jspiner.epub_viewer.ui.reader.viewer.EpubPagerAdapter
import net.jspiner.epub_viewer.ui.reader.viewer.VerticalViewPager
import net.jspiner.epubstream.EpubStream
import net.jspiner.epubstream.dto.ItemRef
import net.jspiner.epubstream.dto.NavPoint
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class ReaderViewModel : BaseViewModel() {

    var viewerTypeStrategy: ViewerTypeStrategy = ScrollTypeStrategy(this)
        private set

    val extractedEpub: Epub = Epub()

    private lateinit var file: File
    private val navPointLocationMap: HashMap<String, ItemRef> = HashMap()

    private val spineSubject: BehaviorSubject<ItemRef> = BehaviorSubject.create()
    private val rawDataSubject = PublishSubject.create<Pair<File, String>>()
    private val toolboxShowSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(true)
    private val pageSubject: BehaviorSubject<Pair<Int, Boolean>> = BehaviorSubject.create()
    private val viewerTypeSubject = BehaviorSubject.createDefault(ViewerType.SCROLL)
    private val pageInfoSubject = BehaviorSubject.create<PageInfo>()

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
                if (content == itemFile.path) return spineItem
            }
            throw RuntimeException("해당 navPoint 를 spine 에서 찾을 수 없음 content : $content")
        }

        for (navPoint in extractedEpub.toc.navMap.navPoints) {
            val navPointContent = navPoint.content.src.split("#")[0]

            navPointLocationMap[navPoint.id] = findMatchItemInSpines(navPointContent)
        }
    }

    fun setSpineItem(itemRef: ItemRef) {
        spineSubject.onNext(itemRef)
    }

    fun getCurrentSpineItem(): Observable<ItemRef> = spineSubject

    fun onPagerItemSelected(pager: VerticalViewPager, adapter: EpubPagerAdapter, position: Int) {
        viewerTypeStrategy.onPagerItemSelected(
            pager, adapter, position
        )
    }

    fun navigateToPoint(navPoint: NavPoint) {
        val itemRef = navPointLocationMap[navPoint.id]
            ?: throw RuntimeException("해당 navPoint 를 찾을 수 없음 id : $navPoint")

        for ((index, item) in extractedEpub.opf.spine.itemrefs.withIndex()) {
            if (item.idRef == itemRef.idRef) {
                if (index != 0) {
                    setCurrentPage(getCurrentPageInfo().pageCountSumList[index - 1], true)
                } else {
                    setCurrentPage(0, true)
                }
                return
            }
        }
        throw RuntimeException("해당 itemRef 를 찾을 수 없음 id : $itemRef")
    }

    fun toManifestItem(itemRef: ItemRef): File {
        return toManifestItem(itemRef.idRef)
    }

    fun toManifestItem(id: String): File {
        val manifestItemList = extractedEpub.opf.manifest.items

        for (item in manifestItemList) {
            if (item.id == id) {
                return extractedEpub.extractedDirectory.resolve(item.href)
            }
        }
        throw RuntimeException("해당 itemRef 를 manifest 에서 찾을 수 없음 id : $id")
    }

    fun setToolboxVisible(isVisible: Boolean) = toolboxShowSubject.onNext(isVisible)

    fun getToolboxVisible() = toolboxShowSubject

    fun setPageInfo(pageInfo: PageInfo) {
        pageInfoSubject.onNext(pageInfo)
        pageSubject.onNext(0 to false)
    }

    fun getCurrentPageInfo() = pageInfoSubject.value!!

    fun getPageInfo(): Observable<PageInfo> = pageInfoSubject

    fun getCurrentPage(): Observable<Pair<Int, Boolean>> = pageSubject

    fun setCurrentPage(page: Int, needUpdate: Boolean) {
        pageSubject.onNext(page to needUpdate)
    }

    fun getViewerType(): Observable<ViewerType> = viewerTypeSubject

    fun getCurrentViewerType() = viewerTypeSubject.value

    fun setViewerType(viewerType: ViewerType) {
        viewerTypeSubject.onNext(viewerType)

        viewerTypeStrategy = when(viewerType) {
            ViewerType.SCROLL -> ScrollTypeStrategy(this)
            ViewerType.PAGE -> PageTypeStrategy(this)
        }
    }

    fun getRawData(): Observable<Pair<File, String>> = rawDataSubject

    fun setRawData(dataPair: Pair<File, String>) {
        rawDataSubject.onNext(dataPair)
    }
}