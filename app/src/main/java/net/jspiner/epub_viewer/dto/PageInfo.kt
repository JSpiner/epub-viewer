package net.jspiner.epub_viewer.dto

data class PageInfo(
    val deviceHeight: Long,
    val allPage: Int,
    val spinePageMap: MutableMap<String, Page> = HashMap()
) {

    companion object {
        fun create(spinePageMap: MutableMap<String, Page>): PageInfo {
            var allPage = 0
            var allHeight = 0L

            for (page in spinePageMap) {
                allPage += page.value.page
                allHeight += page.value.height
            }
            return PageInfo(
                allHeight,
                allPage,
                spinePageMap
            )
        }
    }
}

data class Page(
    val height: Long,
    val page: Int
)