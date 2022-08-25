package kr.co.lina.ga.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * HTTP 통신 서비스
 */
interface RtfService {

    /**
     * 버전체크
     * @param body 앱정보
     * @return Call<RtfModel.AppVersionCheck>
     */
    @Headers("Content-Type: application/json")
    @POST("/api/appVersionCheck")
    fun appVersionCheck(
        // @Body body: JSONObject
        @Body body: RequestBody
    )
            : Call<RtfModel.AppVersionCheck>

    /**
     * 멀티파트 업로드 (멀티)
     * @param partMap
     * @param filelist
     * @return Call<RtfModel.UploadFiles>
     */
    @Multipart
    @POST("/api/files")
    fun uploadFileWithPartMapDynamic(
        @Header("Authorization") authorization: String = "",
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part filelist: List<MultipartBody.Part>
    )
            : Call<RtfModel.UploadFiles>

    /**
     * 멀티파트 업로드 (싱글)
     * @param partMap
     * @param file
     * @return Call<RtfModel.UploadFiles>
     */
    @Multipart
    @POST("/gap/ui/upload?upload_id=fileattach")
//    fun uploadFileWithPartMap(
//        @Header("Authorization") authorization: String = "",
//        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>,
//        @Part file: MultipartBody.Part
//    )
//            : Call<RtfModel.UploadFiles>
    fun uploadFileWithPartMap(
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part
    )
            : Call<ResponseBody>

}