package kr.co.lina.ga.retrofit

import com.google.gson.GsonBuilder
import kr.co.lina.ga.ServerUrls
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * HTTP 통신 클라이언트
 * @property rtf Retrofit 객체 설정
 */
object RetrofitClient {
//    private val rtf = Retrofit.Builder()
//        .baseUrl(WaConfig.URL_API)
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()


    /**
     * Retrofit 서비스 생성
     * @return RtfService
     */
    fun getService(): RtfService {
        val url = ServerUrls.MAIN_DOMAIN
        val rtf = Retrofit.Builder()
//            .baseUrl(WaConfig.URL_API)
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return rtf.create(RtfService::class.java)
    }

    fun getService1() : RtfService {
        val url = ServerUrls.MAIN_DOMAIN

        val rtf1 = Retrofit.Builder()
            .baseUrl(url)
//            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return rtf1.create(RtfService::class.java)
    }
}