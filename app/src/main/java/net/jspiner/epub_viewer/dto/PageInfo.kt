package net.jspiner.epub_viewer.dto

data class PageInfo(
    val deviceHeight: Long,
    val allPage: Int,
    val spinePageList: List<Page> = ArrayList()
) {

    companion object {
        fun create(spinePageList: List<Page>): PageInfo {
            var allPage = 0
            var allHeight = 0L

            for (page in spinePageList) {
                allPage += page.page
                allHeight += page.height
            }
            return PageInfo(
                allHeight,
                allPage,
                spinePageList
            )
        }
    }
}

data class Page(
    val height: Long,
    val page: Int
)