package cn.booslink.llm.common.model

import cn.booslink.llm.common.model.enums.AIUIIntent
import cn.booslink.llm.common.model.enums.Category
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class CBMSemantic(
    val answer: Answer?,
    val category: Category?,
    val data: Map<String, Any>?,
    @SerializedName("dialog_stat")
    val dialogStat: String?,
    val intentType: String?,
    val rc: Int?,
    @SerializedName("save_history")
    val saveHistory: Boolean?,
    val semantic: List<Semantic>?,
    val semanticType: Int?,
    val service: String?,
    val shouldEndSession: String?,
    val sid: String?,
    val text: String?,
    val uuid: String?,
    val version: String?
) {
    fun getResponse(gson: Gson): UIResponse {
        val result = data?.get("result")
        return result?.let {
            val resultJson = gson.toJson(it)
            when (category) {
                Category.WEATHER -> {
                    val weatherList = gson.fromJson<List<Weather>>(resultJson, object : TypeToken<List<Weather>>() {}.type)
                    UIResponse.weatherData(category, weatherList)
                }

                Category.CONTROL -> UIResponse.withCategory(category)
                else -> UIResponse.empty()
            }
        } ?: UIResponse.empty()
    }
}

data class Answer(val text: String?, val type: String?)

class Semantic(
    @SerializedName("entrypoint") val entryPoint: String?,
    val intent: AIUIIntent?,
    val score: Int?,
    val slots: List<Slot>,
    val template: String?
)

data class Slot(val name: String?, val normValue: String?, val value: String?)


