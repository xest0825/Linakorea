package kr.co.lina.ga.photogallery

import android.content.ContentResolver
import android.database.Cursor
import kr.co.lina.ga.photogallery.model.Item
import kr.co.lina.ga.utils.WaLog

/**
 * 이미지 업로드 미리보기 선택시  이미지 경로 확인
 * @property tag Log 태그
 * @property Log Log
 */
internal class ImagesDataSource(private val contentResolver: ContentResolver) {
    val tag = "ImagesDataSource"
    val Log = WaLog

    /**
     * 갤러리 이미지 로딩
     * @param page 페이지
     * @return ArrayList<Item>
     */
    fun loadAlbumImages(page: Int): ArrayList<Item> {
        Log.i(tag, "loadAlbumImages:")
        val offset = page * PAGE_SIZE
        val list: ArrayList<Item> = arrayListOf()
        var cursor: Cursor? = null

        try {
            cursor = contentResolver.query(
                cursorUri,
                arrayOf(ID_COLUMN, PATH_COLUMN),
                null,
                null,
                "$ORDER_BY LIMIT $PAGE_SIZE OFFSET $offset"
            )
            cursor?.isAfterLast ?: return list
            cursor.doWhile {
                val image = cursor.getString((cursor.getColumnIndex(PATH_COLUMN)))
                list.add(Item(image, 0))
            }
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }
        return list
    }
}