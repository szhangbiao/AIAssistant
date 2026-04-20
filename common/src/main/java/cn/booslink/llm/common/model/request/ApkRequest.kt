package cn.booslink.llm.common.model.request

import com.google.gson.annotations.SerializedName

data class ApkRequest(
    @SerializedName("pkg_name") val pkgName: String,
    val source: String = "booslink",
    val channel: String = "AI"
) {
    companion object {
        fun create(pkgName: String): ApkRequest {
            return ApkRequest(pkgName)
        }
    }
}
