package kr.co.lina.ga.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 멀티 파일 업로드 모델
 */
class MultiUploadModel {

    /**
     * 멀티 업로드 데이터
     * @property totalSize 총사이즈
     * @property totalCount 총갯수
     * @property limitSize 최대사이즈
     * @property limitCount 최대갯수
     * @property ref_seq index
     * @property claim_seq index
     * @property attach_gbn 구분
     * @property JSON_WEB_TOKEN web token
     * @property mb_id id
     */
    @Parcelize
    data class MultiUpload(
        val totalSize: String,
        val totalCount: String,
        val limitSize: String,
        val limitCount: String,
        val ref_seq: String,
        val claim_seq: String,
        val attach_gbn: String,
        val JSON_WEB_TOKEN: String,
        val mb_id: String
    ) : Parcelable

}