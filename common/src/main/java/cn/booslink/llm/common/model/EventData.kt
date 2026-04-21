package cn.booslink.llm.common.model

import cn.booslink.llm.common.model.enums.AIUITag
import cn.booslink.llm.common.model.enums.CBMSub
import cn.booslink.llm.common.model.enums.Category
import com.google.gson.annotations.SerializedName

data class EventData(
    val text: IATText?,
    @SerializedName("event") val event: SdkResponse<CBMEvent>?,
    @SerializedName("cbm_tidy") val cbmTidy: SdkResponse<CBMTidy>?,
    @SerializedName("cbm_semantic") val cbmSemantic: SdkResponse<CBMSemantic>?,
    @SerializedName("cbm_tool_pk") val cbmToolPK: SdkResponse<CBMToolPK>?,
    val nlp: SdkResponse<String>?,
    var sub: CBMSub? = null,
    var tag: AIUITag? = null,
    var response: UIResponse? = null
) {
    companion object {
        fun empty() = EventData(null, null, null, null, null, null)
    }

    fun isEmpty(): Boolean = text == null && cbmTidy == null && cbmSemantic == null && cbmToolPK == null && nlp == null

    fun copyIat(text: IATText) = EventData(text, event, cbmTidy, cbmSemantic, cbmToolPK, nlp, sub, tag, response)

    fun copyTidy(cbmTidy: SdkResponse<CBMTidy>) = EventData(text, event, cbmTidy, cbmSemantic, cbmToolPK, nlp, sub, tag, response)

    fun copySemantic(cbmSemantic: SdkResponse<CBMSemantic>, response: UIResponse) = EventData(text, event, cbmTidy, cbmSemantic, cbmToolPK, nlp, sub, tag, response)

    fun copyNlp(nlp: SdkResponse<String>) = EventData(text, event, cbmTidy, cbmSemantic, cbmToolPK, nlp, sub, tag, response)
}

data class IATText(val ls: Boolean?, val pgs: String?, val rg: List<Int>?, val sn: Int?, val ws: List<WS>?) {
    fun getIATVoice(): String? {
        return ws?.joinToString("") { it.cw?.joinToString("") { cw -> cw.w.toString() }.toString() }
    }
}

data class WS(val bg: Int?, val cw: List<CW>?)
data class CW(val ph: String?, val sc: Int?, val w: String?)

data class SdkResponse<T>(
    val compress: String?,
    val encoding: String?,
    val format: String?,
    val parameter: Parameter?,
    val seq: Int?,
    val status: Int?,
    val text: T?
)

data class Parameter(val loc: Loc?, @SerializedName("unique_id") val uniqueId: String?)

data class Loc(val ability: String?, val intent: Int?, @SerializedName("unique_id") val uniqueId: String?)

// {\"query\":\"今天天气怎么样\",\"intent\":[{\"index\":0,\"value\":\"今天天气怎么样\"}]}

data class CBMTidy(val query: String?, val intent: List<VoiceIntent>?)

data class VoiceIntent(val index: Int?, val value: String?)

// {\"pk_type\":\"cbm_semantic\",\"pk_source\":{\"domain\":\"weather\"},\"tool\":{}}

data class CBMToolPK(
    @SerializedName("pk_type") val pkType: String?,
    @SerializedName("pk_source") val pkSource: PKSource?,
    val tool: Map<String, String>?
)

data class PKSource(val domain: String?)

data class UIResponse(
    val category: Category,
    val weathers: List<Weather>? = null,
    val sleepType: Int? = -1
) {
    companion object {
        fun empty() = UIResponse(Category.UNKNOWN)
        fun withCategory(category: Category) = UIResponse(category)
        fun withSleep(sleepType: Int) = UIResponse(Category.SLEEP, null, sleepType)
        fun weatherData(category: Category, weathers: List<Weather>?) = UIResponse(category, weathers)
    }

    fun isEmpty(): Boolean {
        return category == Category.UNKNOWN
    }
}

data class CBMEvent(val type: String, val key: String, val data: String)


