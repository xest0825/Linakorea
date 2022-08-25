package kr.co.lina.ga.photogallery

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/** 이미지 Path */
val cursorUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
/** 이미지 정렬 방식 */
const val ORDER_BY = MediaStore.Images.Media.DATE_TAKEN + " DESC"
//const val DISPLAY_NAME_COLUMN = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
/** 이미지 주소 ID */
const val ID_COLUMN = MediaStore.Images.Media._ID
/** 이미지 Path Column */
const val PATH_COLUMN = MediaStore.Images.Media.DATA
/** Page Size */
const val PAGE_SIZE = 20

/** images 상수 */
const val IMAGES = "images"
/** albums 상수 */
const val ALBUMS = "albums"
/** photo_path 상수 */
const val PHOTO_PATH = "photo_path"
/** album_pos 상수 */
const val ALBUM_POS = "album_pos"
/** page 상수 */
const val PAGE = "page"
/** selected_album 상수 */
const val SELECTED_ALBUM = "selected_album"
/** selected_images 상수 */
const val SELECTED_IMAGES = "selected_images"
/** curren_selection 상수 */
const val CURRENT_SELECTION = "curren_selection"
/** limit 상수 */
const val LIMIT = "limit"
/** limit 상수 */
const val DISABLE_CAMERA = "limit"

/** image_path 상수 */
const val IMAGE_PATH = "image_path"

/** 이미지 Url 가져오기 */
fun Cursor.doWhile(action: () -> Unit) {
    this.use {
        if (this.moveToFirst()) {
            do {
                action()
            } while (this.moveToNext())
        }
    }
}

/**
 * 임시 이미지파일 생성
 * @param context
 * @return File
 */
@Throws(IOException::class)
internal fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )
    return image
}