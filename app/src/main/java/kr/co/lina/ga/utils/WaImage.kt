package kr.co.lina.ga.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.webkit.ValueCallback
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import kr.co.lina.ga.BuildConfig
import kr.co.lina.ga.Model.MultiUploadModel
import kr.co.lina.ga.R
import kr.co.lina.ga.photogallery.GalleryActivity
import kr.co.lina.ga.retrofit.RetrofitClient
import kr.co.lina.ga.retrofit.RtfModel
import kr.co.lina.ga.scanner.ImageCropActivity
import kr.co.lina.ga.ui.main.MainActivity
import kr.co.lkins.EHB.permission.PermissionFactory
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class WaImage {
    private val tag = "WaImage"
    private val Log = WaLog

    private var parent : ImageCropActivity ? = null

    /**
     * 이미지파일 생성
     * @param context
     * @return File
     */
    @SuppressLint("SimpleDateFormat")
    fun createImageFile(context: Context): File {
        Log.i(tag, "createImageFile:")
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        // 파일 프로바이더로 변경
        //val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DCIM)
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        //val storageDir = context.externalCacheDir
        if (!storageDir!!.exists()) storageDir.mkdir()
        val image = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        image.deleteOnExit()
        return image
    }

    /**
     * 리사이즈 이미지
     * @param context
     * @param destWidth
     * @param imgFile
     * @return String file 경로
     */
    fun resizeImageFile(context: Context, destWidth: Int, imgFile: File): String {
        Log.i(tag, "resizeImageFile:")
        val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        val origWidth = bitmap.width
        val origHeight = bitmap.height
        if (origWidth > destWidth) {
            val destHeight = origHeight / (origWidth / destWidth)
            val bitmap2 = Bitmap.createScaledBitmap(bitmap, destWidth, destHeight, false)
            val outStream = ByteArrayOutputStream()
            bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            val file = createImageFile(context)
            file.createNewFile()
            val fos = FileOutputStream(file)
            fos.write(outStream.toByteArray())
            fos.close()
            return "file:" + file.absolutePath
        } else {
            return "file:" + imgFile.absoluteFile
        }
    }

    private var mProgress: AlertDialog? = null
    private var progressBar: ProgressBar? = null

    /**
     * 로딩 시작
     * @param context
     */
    private fun startLoading(context: Context) {
        Log.i(tag, "startLoading:")
        var locale: String = WaSharedPreferences(context).readPrefer("language").toString()
//        var uploadStr = Strings.getString(context, locale).get("gallery_main_upload")
        var uploadStr = context.getString(R.string.gallery_main_upload)
        //Handler().postDelayed({
        //    mProgress = WaProgressDialog.setProgressDialog(context, uploadStr!!)
        //    mProgress?.show()
        //}, 0)
    }

    /**
     * 로딩 중지
     */
    private fun stopLoading() {
        Log.i(tag, "stopLoading:")
        //Handler().postDelayed({
        //    if (mProgress != null)
        //        mProgress?.dismiss()
        //}, 0)
        parent!!.myProgressBar!!.isVisible = false
    }

    /**
     * 업로드 파일 - 카메라 촬영 후 처리 로직 - Scanner
     * @return MultipartBody.Part.createFormData()
     */
    private fun prepareFilePart(
        context: Context,
        partName: String,
        fileUri: Uri,
        filePath: String
    ): MultipartBody.Part {
        Log.i(tag, "prepareFilePart: partName:$partName, fileUri:$fileUri, filePath:$filePath")
        // 다이렉트 읽기
        val file: File? = File(filePath)

        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse(context.contentResolver.getType(fileUri)!!),
            file!!
        )
        return MultipartBody.Part.createFormData(partName, file.name, requestBody)
    }

    /**
     * 업로드 파일 - 카메라 촬영 후 처리 로직 - Scanner
     * @param activity
     * @param context
     * @param fileUri
     * @param filePath
     * @param uploadData
     */
    fun uploadFile(
        activity: ImageCropActivity,
        context: Context,
        fileUri: Uri,
        filePath: String,
        uploadData: MultiUploadModel.MultiUpload,
        type:Int
    ) {
        Log.i(tag, "uploadFile: fileUri:$fileUri, filePath:$filePath")

        this.parent = activity

        // file part
        val body: MultipartBody.Part = prepareFilePart(context, "files", fileUri, filePath)
//        //
//        val paramReqSeq: RequestBody = RequestBody.create(MultipartBody.FORM, uploadData.ref_seq)
//        val paramClaimSeq: RequestBody =
//            RequestBody.create(MultipartBody.FORM, uploadData.claim_seq)
//        val paramAttachGbn: RequestBody =
//            RequestBody.create(MultipartBody.FORM, uploadData.attach_gbn)
//
        val requestMap = emptyMap<String, RequestBody>().toMutableMap()
//        requestMap["ref_seq"] = paramReqSeq
//        requestMap["claim_seq"] = paramClaimSeq
//        requestMap["attach_gbn"] = paramAttachGbn

        val funCd = RequestBody.create(MultipartBody.FORM, "save")
        requestMap["funCd"] = funCd

        val subfPath = RequestBody.create(MultipartBody.FORM, "ga")
        requestMap["subFPath"] = subfPath

        startLoading(context)

        RetrofitClient.getService1()
            .uploadFileWithPartMap( requestMap, body)
//            .enqueue(object : Callback<RtfModel.UploadFiles> {
                .enqueue(object : Callback<ResponseBody> {
//                override fun onFailure(call: Call<RtfModel.UploadFiles>, t: Throwable) {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(tag, "onFailure: t$t")
                    stopLoading()
                    val result = t.localizedMessage
                    processUploadResult(activity, result, type)
                }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        stopLoading()
                        val result = String(response.body()!!.bytes())
                        Log.d("xxx", result)
                        processUploadResult(activity, result, type)
                    }
                })
    }

    /**
     * 업로드 결과 처리
     * @param activity CropActivity
     * @param uploadState
     */
    private fun processUploadResult(activity: ImageCropActivity, uploadState: String, type:Int) {
        Log.i(tag, "processUploadResult:")
        val intent = Intent()
        intent.putExtra("type", type)
        intent.putExtra("upload_state", uploadState)
        activity.setResult(Activity.RESULT_OK, intent)
        activity.finish()
    }


}
