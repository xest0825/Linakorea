package kr.co.lina.ga.scanner

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import kotlinx.android.synthetic.main.activity_image_crop.*
import kr.co.lina.ga.BuildConfig
import kr.co.lina.ga.Model.MultiUploadModel
import kr.co.lina.ga.R
import kr.co.lina.ga.ui.main.MainActivity
import kr.co.lina.ga.utils.WaImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ImageCropActivity : AppCompatActivity() {

    private lateinit var mUpload: MultiUploadModel.MultiUpload

    private lateinit var mFilePath: String
    private lateinit var mCropFile: File
    private lateinit var mCropFileUri: Uri
    lateinit var mainContext: Context

    var rotatedBitmap: Bitmap? = null
    var croppedBitmap: Bitmap? = null

    var myProgressBar: ProgressBar? = null

    companion object {
        private const val FILE_DIR = "file_dir"
        private const val FILE_TYPE = "type"
        private const val COMPRESS_VALUE = "compress"

        var compress_value = 40
        fun newIntent(context: Context, selectedFilePath: String) =
            Intent(context, ImageCropActivity::class.java).putExtra(FILE_DIR, selectedFilePath)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop)

        myProgressBar = findViewById<ProgressBar>(R.id.progressBar1)

        // upload_data
        mUpload = intent.getParcelableExtra<MultiUploadModel.MultiUpload>("upload_data")!!
        Log.i(TAG, "mMultiUpload:$mUpload")

        // 1
        val filePath = intent.extras?.getString(FILE_DIR)!!
        val type = intent.extras?.getInt(FILE_TYPE)!!
        compress_value = intent.extras?.getInt(COMPRESS_VALUE)!!

        // 2
        // rotate
        val orientation: Int = getOrientation(filePath)

        val bitmap = assetToBitmap(filePath)

        rotatedBitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
            else -> bitmap
        }

        document_scanner.setOnLoadListener { loading ->
            this.myProgressBar!!.isVisible = loading
        }
        document_scanner.setImage(rotatedBitmap!!)
        btnImageCrop.setOnClickListener {
            //lifecycleScope.launch {
            this.myProgressBar!!.isVisible = true
            val image = document_scanner.getCroppedImage()
            croppedBitmap = image
            this.myProgressBar!!.isVisible = false
            result_image.isVisible = true
            crop_upload_btn.isVisible = true
            image_crop_layer.visibility = View.GONE
            result_image.setImageBitmap(image)
            //}

            save()

            Log.i(TAG, "processUpload:")

        }

        btnImageCancel.setOnClickListener {
            val mIntent = Intent(this, MainActivity::class.java)
            setResult(RESULT_CANCELED, mIntent)
            finish()
        }

        crop_upload_btn.setOnClickListener {
            myProgressBar!!.isVisible = true
            WaImage().uploadFile(
                this,
                this,
                mCropFileUri,
                mFilePath,
                mUpload,
                type
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getOrientation(filePath: String) : Int {
        var ei: ExifInterface?
        var orientation: Int = 0

        if (filePath.contains("file:") == true){
            val absPath = filePath.removePrefix("file:")
            ei = ExifInterface(absPath)

            orientation = ei!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        }
        else {
            var ins : InputStream?
            try {
                ins = contentResolver.openInputStream(Uri.parse(filePath))
                ei = ExifInterface(ins!!)

                orientation = ei!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            } catch (e: IOException) {
                // Handle any errors
            }
        }
        return orientation
    }

    fun assetToBitmap(file: String): Bitmap =
        contentResolver.openInputStream(Uri.parse(file)).run {
            BitmapFactory.decodeStream(this)
        }

//    fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
//        val matrix = Matrix()
//        matrix.postRotate(angle)
//        return Bitmap.createBitmap(
//            source, 0, 0, source.width, source.height,
//            matrix, true
//        )
//    }

    private fun save() {
        val IMAGES_DIR = "LinaScranner"
        val dir = File(Environment.getExternalStorageDirectory(), IMAGES_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }

//        val cropPic = rotatedBitmap
        val cropPic = croppedBitmap
        if (null != cropPic) {
            // 파일 생성 위치 정의  - 패키지쪽으로 옴김
            //val file = File(dir, "crop_${SystemClock.currentThreadTimeMillis()}.jpeg")
            mCropFileUri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                WaImage().createImageFile(this).also { file ->
                    mCropFile = file
                }
            )

            val outStream = FileOutputStream(mCropFile)
            cropPic.compress(Bitmap.CompressFormat.JPEG, compress_value, outStream) // 2022.7.13 (100->40)
            outStream.flush()
            outStream.close()

            // addImageToGallery(mCropFile.absolutePath, this.context)
            // Toast.makeText(context, "picture saved, path: ${mCropFile.absolutePath}", Toast.LENGTH_SHORT).show()

            mFilePath = mCropFile.absolutePath
        }
    }
}
