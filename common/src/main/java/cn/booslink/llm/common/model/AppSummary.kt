package cn.booslink.llm.common.model

import com.google.gson.annotations.SerializedName

data class AppSummary(val name: String, val nickname: String, @SerializedName("pkg_name") val pkgName: String, val entry: String) {
    companion object {
        fun empty() = AppSummary("", "", "", "")
    }

    fun isEmpty(): Boolean = pkgName.isEmpty()
}
