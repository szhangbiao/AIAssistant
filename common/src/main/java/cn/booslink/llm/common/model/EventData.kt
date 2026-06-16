package cn.booslink.llm.common.model

import cn.booslink.llm.common.model.enums.AIUIIntent
import cn.booslink.llm.common.model.enums.AIUITag
import cn.booslink.llm.common.model.enums.CBMSub
import cn.booslink.llm.common.model.enums.Category
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

data class EventData(
    var id: String?,
    val text: IATText?,
    @SerializedName("event") val event: SdkResponse<CBMEvent>?,
    @SerializedName("cbm_tidy") val cbmTidy: SdkResponse<CBMTidy>?,
    @SerializedName("cbm_semantic") val cbmSemantic: SdkResponse<CBMSemantic>?,
    @SerializedName("cbm_tool_pk") val cbmToolPK: SdkResponse<CBMToolPK>?,
    val nlp: SdkResponse<String>?,
    var sub: CBMSub? = null,
    var tag: AIUITag? = null,
    var response: UIResponse? = null,
    var semanticHandled: Boolean = false,
) {
    companion object {
        fun empty() = EventData(null, null, null, null, null, null, null, semanticHandled = false)

        fun withId(id: String?) = EventData(id, null, null, null, null, null, null, semanticHandled = false)
    }

    fun isEmpty(): Boolean = id == null && text == null && event == null && cbmTidy == null && cbmSemantic == null && cbmToolPK == null && nlp == null

    fun copyIat(text: IATText) = EventData(id, text, event, cbmTidy, cbmSemantic, cbmToolPK, nlp, sub, tag, response, semanticHandled)

    fun copyTidy(cbmTidy: SdkResponse<CBMTidy>) =
        EventData(id, text, event, cbmTidy, cbmSemantic, cbmToolPK, nlp, sub, tag, response, semanticHandled)

    fun copySemantic(cbmSemantic: SdkResponse<CBMSemantic>, response: UIResponse): EventData {
        val semantic = cbmSemantic.text?.semantic?.firstOrNull()
        val targetCategory = response.category == Category.PAGE_CONTROL || response.category == Category.KSONG
        val targetIntent = semantic?.intent == AIUIIntent.PAGE_OPEN || semantic?.intent == AIUIIntent.OPEN_SCORE || semantic?.intent == AIUIIntent.CLOSE_SCORE
        val queryText = cbmTidy?.text?.query
        if (targetCategory && targetIntent && semantic.slots.isNotEmpty()) {
            val slot = semantic.slots.firstOrNull()
            val targetName = "page" == slot?.name || "score" == slot?.name
            if (targetName && slot.normValue?.isNotEmpty() == true && slot.value?.isNotEmpty() == true && slot.normValue != slot.value) {
                cbmTidy?.text?.query = cbmTidy.text.query?.replace(slot.value, slot.normValue)
            }
        } else if ((response.category == Category.CONTROL || response.category == Category.APP) && semantic?.intent == AIUIIntent.EXIT && "小飞退出" == queryText) {
            semantic.intent = AIUIIntent.EXIT_AGENT
        } else if (response.category == Category.UNKNOWN && "视频全屏" == queryText) {
            response.category = Category.KSONG
            if (semantic == null) {
                cbmSemantic.text?.semantic = listOf(Semantic.newIntent(AIUIIntent.KSONG_FULLSCREEN))
            } else {
                semantic.intent = AIUIIntent.KSONG_FULLSCREEN
            }
        }
        return EventData(id, text, event, cbmTidy, cbmSemantic, cbmToolPK, nlp, sub, tag, response, !response.isWeatherEmpty())
    }

    fun copyNlp(nlp: SdkResponse<String>) = EventData(id, text, event, cbmTidy, cbmSemantic, cbmToolPK, nlp, sub, tag, response, semanticHandled)
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

data class CBMTidy(var query: String?, val intent: List<VoiceIntent>?)

data class VoiceIntent(val index: Int?, val value: String?)

// {\"pk_type\":\"cbm_semantic\",\"pk_source\":{\"domain\":\"weather\"},\"tool\":{}}

data class CBMToolPK(
    @SerializedName("pk_type") val pkType: String?,
    @SerializedName("pk_source") val pkSource: PKSource?,
    val tool: Map<String, String>?
)

data class PKSource(val domain: String?)

data class UIResponse(
    var category: Category,
    val queryDate: String? = null,
    val weathers: List<Weather>? = null,
    val sleepType: Int? = -1
) {
    companion object {
        fun empty() = UIResponse(Category.UNKNOWN)
        fun withCategory(category: Category) = UIResponse(category)
        fun withSleep(sleepType: Int) = UIResponse(Category.SLEEP, null, null, sleepType)
        fun weatherData(category: Category, semantic: Semantic?, weathers: List<Weather>?): UIResponse {
            var queryDate: String? = null
            if (semantic?.slots?.isNotEmpty() == true) {
                val dateJson: String? = semantic.slots.firstOrNull { slot -> slot.name == "datetime" }?.normValue
                val rawDate = dateJson?.let { JsonParser.parseString(it).asJsonObject.get("datetime")?.asString }
                if (rawDate?.isNotEmpty() == true) {
                    val pattern: Pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})")
                    val matcher: Matcher = pattern.matcher(rawDate)
                    if (matcher.find()) {
                        queryDate = matcher.group(1)
                    }
                }
            }
            return UIResponse(category, queryDate, weathers)
        }
    }

    fun isWeatherEmpty(): Boolean {
        return weathers == null || weathers.isEmpty()
    }

    fun queryWeatherInvalid(): Boolean {
        if (queryDate.isNullOrEmpty() || isWeatherEmpty()) return true
        try {
            val dateTime: DateTime = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(queryDate)
            return dateTime < weathers?.firstOrNull()?.date || dateTime > weathers?.lastOrNull()?.date
        } catch (e: IllegalArgumentException) {
            return true
        }
    }

    fun getWeatherTTSSpeechText(): String? {
        if (queryDate.isNullOrEmpty() || isWeatherEmpty()) return null
        val queryDayWeather: Weather? = weathers?.firstOrNull { weather -> queryDate == weather.date?.toString("yyyy-MM-dd") }
        return queryDayWeather?.let {
            "${it.city}${it.dateForVoice}天气${it.weather}，${it.weatherDescription}"
        }
    }

    fun getQueryDayWeather(): Weather? {
        if (queryDate.isNullOrEmpty() || isWeatherEmpty()) return null
        return weathers?.firstOrNull { weather -> queryDate == weather.date?.toString("yyyy-MM-dd") }
    }
}

data class CBMEvent(val type: String, val key: String, val data: String)


