package kr.co.lina.ga.photogallery

import android.content.ContentResolver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.lina.ga.photogallery.model.Item
import kr.co.lina.ga.utils.WaLog

/**
 * 이미지 업로드 미리보기 모델
 * @property tag Log 태그
 * @property Log Log
 * @property showOverLimit limit 초과 여부
 * @property mLastAddedImages 마지막 추가 이미지
 * @property mPage 페이지
 * @property mCurrentSelection 선택한 이미지 수
 * @property mLimit limit 수
 * @property mImageDataSource ImagesDataSource
 * @property contentResolver ContentResolver
 * @property getCurrentSelection() 선택한 이미지 수 가져오기
 * @property isOverLimit() limit 초과 확인
 * @property mSelectedList 선택한 이미지 리스트
 * @property mNotifyPosition MutableLiveData<Int>
 * @property mDoneEnabled MutableLiveData<Boolean>
 */
//class GalleryViewModel (private val savedStateHandle: SavedStateHandle) : ViewModel() {
class GalleryViewModel() : ViewModel() {
    val tag = "GalleryViewModel"
    val Log = WaLog

    internal var showOverLimit = MutableLiveData<Boolean>()
    var mLastAddedImages = MutableLiveData<ArrayList<Item>>()
    var mPage = 0
    private var mCurrentSelection: Int = 0
    private var mLimit = 5

    private lateinit var mImageDataSource: ImagesDataSource
    private lateinit var contentResolver: ContentResolver
    private fun getCurrentSelection() = mCurrentSelection
    private fun isOverLimit() = mCurrentSelection >= mLimit

    private var mSelectedList = hashMapOf<String, Item>()
    private var mNotifyPosition = MutableLiveData<Int>()
    private var mDoneEnabled = MutableLiveData<Boolean>()

    /** 초기화 */
    internal fun init(contentResolver: ContentResolver) {
        this.contentResolver = contentResolver
        this.mImageDataSource = ImagesDataSource(this.contentResolver)
    }

    /**
     * limit 갯수 설정하기
     * @param limit limit 갯수
     */
    fun setLimit (limit : Int) {
        mLimit = limit
    }

    /**
     * 선택한 이미지 사이즈 가져오기
     * @return Int
     */
    fun getSelectSize(): Int {
        Log.i(tag, "getSelectSize:")
        return mSelectedList.size
    }

    /**
     * 선택한 이미지 리스트 가져오기
     * @return HashMap<String, Item>
     */
    fun getSelectedList(): HashMap<String, Item> {
        return mSelectedList
    }

    /** 이미지 더보기 */
    internal fun loadImagesData() {
        loadImages()
    }

    /** 이미지 더보기 */
    internal fun loadMoreImages() {
        loadImages(true)
    }

    /**
     * 이미지 더보기
     * @param isLoadMore 더보기 여부
     */
    private fun loadImages(isLoadMore: Boolean = false) {
        if (isLoadMore) {
            mPage += 1
        } else {
            mPage = 0
        }
        viewModelScope.launch() {
            val images = getImages()
            mLastAddedImages.value = images
        }
    }

    /** 이미지 가져오기 */
    private suspend fun getImages() = withContext(Dispatchers.Default) {
        mImageDataSource.loadAlbumImages(mPage)
    }

    /** 이미지영역에 이미지 표시 */
    internal fun setImageSelection(position: Int, adapterImageItem: ArrayList<Item>?) {
        Log.i(tag, "setImageSelection: position:$position Item:${adapterImageItem.toString()}")
        if (adapterImageItem.isNullOrEmpty()) return

        val imageItem = adapterImageItem[position]
        if (imageItem.selected == 0) {
            if (isOverLimit()) {
                showOverLimit.value = true
                return
            }
            mCurrentSelection++
            imageItem.selected = mCurrentSelection
            mSelectedList[imageItem.imagePath] = imageItem
        } else {
            for ((i, mItem) in adapterImageItem.withIndex()) {
                if (mItem.selected > imageItem.selected) {
                    mItem.selected--
                    mNotifyPosition.value = i
                }
            }
            imageItem.selected = 0
            mCurrentSelection--
            mSelectedList.remove(imageItem.imagePath)
        }
        mNotifyPosition.value = position
        mDoneEnabled.value = getCurrentSelection() > 0
    }

}