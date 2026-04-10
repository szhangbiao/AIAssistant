package cn.booslink.llm.common.model

import cn.booslink.llm.common.model.enums.Category
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class CBMSemantic(
    val answer: Answer?,
    val category: String?,
    val data: Map<String, Any>?,
    @SerializedName("dialog_stat")
    val dialogStat: String?,
    val rc: Int?,
    @SerializedName("save_history")
    val saveHistory: Boolean?,
    val semantic: List<Semantic>?,
    val service: String?,
    val shouldEndSession: String?,
    val sid: String?,
    val text: String?,
    val uuid: String?,
    val version: String?
) {
    fun getResponse(gson: Gson): UIResponse? {
        val result = data?.get("result")
        return result?.let {
            val resultJson = gson.toJson(it)
            when (val categoryEnum: Category = Category.fromString(category)) {
                Category.WEATHER -> {
                    val weatherList = gson.fromJson<List<Weather>>(resultJson, object : TypeToken<List<Weather>>() {}.type)
                    UIResponse.weatherData(categoryEnum, weatherList)
                }

                else -> null
            }
        }
    }
}

data class Answer(val text: String?, val type: String?)

class Semantic(val intent: String, val slots: List<Slot>)

data class Slot(val name: String?, val normValue: String?, val value: String?)


