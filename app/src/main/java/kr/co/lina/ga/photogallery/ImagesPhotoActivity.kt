package kr.co.lina.ga.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import kr.co.lina.ga.R
import kr.co.lina.ga.utils.WaLog
import uk.co.senab.photoview.PhotoViewAttacher
import java.io.File

/**
 * 촬영후 이미지 확인하는 화면
 * @property tag Log 태그
 * @property Log Log
 */
class ImagesPhotoActivity : LocalizationActivity() {
    val tag = "ImagesPhotoActivity"
    val Log = WaLog
    lateinit var mPhotoView: PhotoViewAttacher
    private lateinit var imagesphoto_iv_image: AppCompatImageView
    private lateinit var imagesphoto_btn_confirm: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images_photo)
        Log.i(tag, "onCreate:")

        val mImagePath = intent.getStringExtra(IMAGE_PATH)
        val mImageFile = File(mImagePath!!)
        var mBmpImage: Bitmap? = null
        if (mImageFile.exists()) {
            mBmpImage = BitmapFactory.decodeFile(mImageFile.absolutePath)
        }
        imagesphoto_iv_image = findViewById(R.id.imagesphoto_iv_image)
        imagesphoto_btn_confirm = findViewById(R.id.imagesphoto_btn_confirm)

        imagesphoto_iv_image.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                imagesphoto_iv_image.viewTreeObserver.removeOnGlobalLayoutListener(this)

                imagesphoto_iv_image.setImageBitmap(mBmpImage)
                mPhotoView = PhotoViewAttacher(imagesphoto_iv_image)
                mPhotoView.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        })

        imagesphoto_btn_confirm.setOnClickListener {
            finish()
        }
    }

    /** Activity 종료전 호출 */
    override fun onDestroy() {
        super.onDestroy()
        Log.i(tag, "onDestroy:")
    }

    override fun onResume() {
        super.onResume()
        Log.i(tag, "onResume:")
    }

}