package cn.booslink.llm.common.model

import com.google.gson.annotations.SerializedName

data class AppUpgrade(val url: String, val md5: String, val version: Int, @SerializedName("icon_url") val iconUrl: String)
