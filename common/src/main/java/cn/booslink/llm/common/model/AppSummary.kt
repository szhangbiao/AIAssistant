package cn.booslink.llm.common.model

import com.google.gson.annotations.SerializedName

data class AppSummary(val name: String, val nickname: String?, @SerializedName("pkg_name") val pkgName: String) {
    companion object {
        fun empty() = AppSummary("", "", "")
    }

    fun isEmpty(): Boolean = pkgName.isEmpty()

    fun findMatch(appName: String) : Boolean {
        if (appName.isBlank()) return false
        
        val searchName = appName.trim().lowercase()
        val targetName = name.lowercase()
        val targetNickname = nickname?.lowercase()
        
        return targetName.contains(searchName) || targetNickname?.contains(searchName) == true
    }
}
