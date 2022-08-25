package kr.co.lina.ga.webcore

/**
 * Bridge Basic Model
 * @property command
 * @property action
 * @property value
 */
data class WcInterfaceModel(
    val command: String,
    val action: String,
    val value: WcInterfaceValuesModel
)

/**
 * Value Data
 * @property totalSize 총사이즈
 * @property totalCount 총카운트
 * @property limitSize 제한사이즈
 * @property limitCount 제한카운트
 * @property ref_seq index
 * @property claim_seq index
 * @property attach_gbn 구분자
 * @property JSON_WEB_TOKEN web token
 * @property mb_id id
 */
data class WcInterfaceValuesModel(

    // camera, gallery file upload
    val totalSize: String,
    val totalCount: String,
    val limitSize: String,
    val limitCount: String,
    val ref_seq: String,
    val claim_seq: String,
    val attach_gbn: String,
    val JSON_WEB_TOKEN: String,
    val mb_id: String,
    val lang: String
)