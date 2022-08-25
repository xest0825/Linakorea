package kr.co.lina.ga.photogallery

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import kr.co.lina.ga.R
import kr.co.lina.ga.photogallery.model.Item
import kr.co.lina.ga.utils.WaLog
import kr.co.lina.ga.utils.WaSharedPreferences

/**
 * 갤러리 이미지 업로드 화면
 * @property tag Log 태그
 * @property Log Log
 * @property context Context
 * @property mRecyclerView RecyclerView
 * @property mMainViewModel GalleryViewModel
 * @property mImagesAdapter ImagesAdapter
 * @property mLoadMoreListener LoadMoreListener
 * @property locale 언어 정보
 * layout : activity_gallery.xml
 */
class GalleryActivity : LocalizationActivity(), LoadMoreListener.OnLoadMoreListener,
    ImagesItemClickListener {
    val tag = "GalleryActivity"
    val Log = WaLog
    private var context: Context? = null
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mMainViewModel: GalleryViewModel
    private var mImagesAdapter: ImagesAdapter? = null
    private var mLoadMoreListener: LoadMoreListener? = null
    private lateinit var locale: String

    private lateinit var gallery_main_title: AppCompatTextView
    private lateinit var  gallery_main_btn_upload: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        context = this
        Log.i(tag, "onCreate:")

        gallery_main_title = findViewById(R.id.gallery_main_title)
        gallery_main_btn_upload = findViewById(R.id.gallery_main_btn_upload)

        var limitCount = intent.getStringExtra("limitCount")
        if (limitCount.isNullOrEmpty()) {
            limitCount = "5"
        }

        locale = WaSharedPreferences(this).readPrefer("language").toString()
//        gallery_main_title.setText(Strings.getString(this, locale).get("gallery_main_title"))
//        gallery_main_btn_upload.setText(Strings.getString(this, locale).get("gallery_main_upload"))
        gallery_main_title.setText(this.getString(R.string.gallery_main_title))
        gallery_main_btn_upload.setText(this.getString(R.string.gallery_main_upload))

        initViews(limitCount.toInt())
    }

    /**
     * 업로드 화면 초기화
     * @param limitCount 첨부가능한 최대 이미지 수
     */
    private fun initViews(limitCount: Int) {
        Log.i(tag, "initViews:")
        mRecyclerView = findViewById(R.id.gallery_recycler_view)
        val layoutManager = GridLayoutManager(this, 3)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.setHasFixedSize(true)
        mImagesAdapter = ImagesAdapter(context!!, this)
        mImagesAdapter!!.items = arrayListOf()
        mRecyclerView.adapter = mImagesAdapter

        mMainViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        mMainViewModel.init(contentResolver)
        mMainViewModel.setLimit(limitCount)

        observe()
        setLoadMoreListener()
        mMainViewModel.loadImagesData()

        gallery_main_btn_upload.setOnClickListener {
            Log.i(tag, "gallery_main_btn_upload:")
            val data = mMainViewModel.getSelectedList()
            val array = data.keys.toTypedArray()

            val intent = intent
            intent.putExtra("selectedList", array)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    /** 더보기 동작 */
    private fun setLoadMoreListener() {
        Log.i(tag, "setLoadMoreListener:")
        if (mLoadMoreListener != null) {
            mRecyclerView.removeOnScrollListener(mLoadMoreListener!!)
        }
        mLoadMoreListener = LoadMoreListener(mRecyclerView.layoutManager as GridLayoutManager)
        mLoadMoreListener?.setOnLoadMoreListener(this@GalleryActivity)
        mRecyclerView.addOnScrollListener(mLoadMoreListener!!)
    }

    /** 이미지 추가 확인 */
    private fun observe() {
        Log.i(tag, "observe:")
        mMainViewModel.mLastAddedImages.observe(this, Observer {
            addImages(it)
        })
        mMainViewModel.showOverLimit.observe(this, Observer {
            showLimitMsg()
        })
    }

    /**
     * 이미지 추가
     * @param it 선택한 이미지들
     */
    private fun addImages(it: ArrayList<Item>) {
        Log.i(tag, "addImages:")
        mLoadMoreListener?.setFinished(false)
        if (it.isNullOrEmpty()) {
            mLoadMoreListener?.setFinished()
            mLoadMoreListener?.setLoaded()
            return
        }
        val isFirstPage = mMainViewModel.mPage == 0
        if (it.size < PAGE_SIZE) {
            mLoadMoreListener?.setFinished()
        }
        val lastPos = mImagesAdapter?.items?.size ?: 0
        if (isFirstPage) {
            mImagesAdapter?.items = it
            mImagesAdapter?.notifyDataSetChanged()
        } else {
            mImagesAdapter?.items?.addAll(it)
            mImagesAdapter?.notifyItemRangeInserted(lastPos, it.size)
        }
        mLoadMoreListener?.setLoaded()
    }

    /** 이미지 갯수 초과시 메시지 팝업 */
    private fun showLimitMsg() {
        Toast.makeText(this, "Limit Msg", Toast.LENGTH_SHORT).show()
    }

    /** 이미지 더보기 */
    override fun onLoadMore() {
        Log.i(tag, "onLoadMore:")
        mMainViewModel.loadMoreImages()
    }

    /**
     * 이미지 선택시 이벤트
     * @param position 선택한 이미지의 인덱스
     */
    override fun onItemClicked(position: Int) {
        Log.i(tag, "onItemClicked: position:$position")
        mMainViewModel.setImageSelection(position, mImagesAdapter?.items)

        //val txt = "업로드 [${mMainViewModel.getSelectSize()}]"
//        val txt = Strings.getString(this, locale).get("gallery_main_upload") + " [${mMainViewModel.getSelectSize()}]"
        val txt = this.getString(R.string.gallery_main_upload) + " [${mMainViewModel.getSelectSize()}]"
//        gallery_main_btn_upload.text = txt

        // 체크 박스 업데이트 동작함
        mImagesAdapter?.notifyItemChanged(position)
    }

    override fun onResume() {
        super.onResume()
        Log.i(tag, "onResume:")
    }

}