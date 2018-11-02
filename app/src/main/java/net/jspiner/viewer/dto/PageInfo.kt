package net.jspiner.viewer.dto

import java.io.Serializable

data class PageInfo(
    val deviceHeight: Long,
    val allPage: Int,
    val spinePageList: List<Page>,
    val pageCountSumList: List<Int>
) : Serializable {

    companion object {
        fun create(spinePageList: List<Page>): PageInfo {
            var allPage = 0
            var allHeight = 0L
            val pageCountSumList = ArrayList<Int>()

            for (page in spinePageList) {
                allPage += page.page
                allHeight += page.height
                pageCountSumList.add(allPage)
            }
            return PageInfo(
                allHeight,
                allPage,
                spinePageList,
                pageCountSumList
            )
        }
    }
}

data class Page(
    val height: Long,
    val page: Int,
    val splitIndexList: List<Long> = ArrayList()
) : Serializable