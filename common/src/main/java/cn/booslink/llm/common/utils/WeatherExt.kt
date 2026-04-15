package cn.booslink.llm.common.utils

import androidx.annotation.DrawableRes
import cn.booslink.llm.common.R
import cn.booslink.llm.common.model.Weather
import cn.booslink.llm.common.model.enums.EmoteState

@DrawableRes
fun Weather.getBigIcon(): Int {
    val processedWeather = preprocessWeather()
    return when (processedWeather) {
        "晴" -> R.drawable.ic_weather_big_sunny
        "雾" -> R.drawable.ic_weather_big_fog
        "雾霾" -> R.drawable.ic_weather_big_smog
        "阴" -> R.drawable.ic_weather_big_overcast
        "多云" -> R.drawable.ic_weather_big_cloudy
        "雨" -> R.drawable.ic_weather_big_rain
        "雷阵雨" -> R.drawable.ic_weather_big_thundershower
        "冻雨" -> R.drawable.ic_weather_big_freezingrain
        "小雨" -> R.drawable.ic_weather_big_smallrain
        "中雨" -> R.drawable.ic_weather_big_moderaterain
        "大雨" -> R.drawable.ic_weather_big_heavyrain
        "暴雨" -> R.drawable.ic_weather_big_rainstorm
        "大暴雨" -> R.drawable.ic_weather_big_greatrain
        "特大暴雨" -> R.drawable.ic_weather_big_torrentialrain
        "阵雨" -> R.drawable.ic_weather_big_shower
        "雪" -> R.drawable.ic_weather_big_snow
        "小雪" -> R.drawable.ic_weather_big_smallsnow
        "中雪" -> R.drawable.ic_weather_big_moderatesnow
        "大雪" -> R.drawable.ic_weather_big_heavysnow
        "暴雪" -> R.drawable.ic_weather_big_greatsnow
        "阵雪" -> R.drawable.ic_weather_big_snowshower
        "雨夹雪" -> R.drawable.ic_weather_big_rainandsnow
        "沙尘暴" -> R.drawable.ic_weather_big_sandstorm
        "强沙尘暴" -> R.drawable.ic_weather_big_strongsandstorm
        "雷雨冰雹" -> R.drawable.ic_weather_big_thunderstormandhail
        "浮尘" -> R.drawable.ic_weather_big_dust
        "扬沙" -> R.drawable.ic_weather_big_yangsha
        else -> -1
    }
}

@DrawableRes
fun Weather.getSmallIcon(): Int {
    val processedWeather = preprocessWeather()
    return when (processedWeather) {
        "晴" -> R.drawable.ic_weather_sunny
        "雾" -> R.drawable.ic_weather_fog
        "雾霾" -> R.drawable.ic_weather_smog
        "阴" -> R.drawable.ic_weather_overcast
        "多云" -> R.drawable.ic_weather_cloudy
        "雨" -> R.drawable.ic_weather_rain
        "雷阵雨" -> R.drawable.ic_weather_thundershower
        "冻雨" -> R.drawable.ic_weather_freezingrain
        "小雨" -> R.drawable.ic_weather_smallrain
        "中雨" -> R.drawable.ic_weather_moderaterain
        "大雨" -> R.drawable.ic_weather_heavyrain
        "暴雨" -> R.drawable.ic_weather_rainstorm
        "大暴雨" -> R.drawable.ic_weather_greatrain
        "特大暴雨" -> R.drawable.ic_weather_torrentialrain
        "阵雨" -> R.drawable.ic_weather_shower
        "雪" -> R.drawable.ic_weather_snow
        "小雪" -> R.drawable.ic_weather_smallsnow
        "中雪" -> R.drawable.ic_weather_moderatesnow
        "大雪" -> R.drawable.ic_weather_heavysnow
        "暴雪" -> R.drawable.ic_weather_greatsnow
        "阵雪" -> R.drawable.ic_weather_snowshower
        "雨夹雪" -> R.drawable.ic_weather_rainandsnow
        "沙尘暴" -> R.drawable.ic_weather_sandstorm
        "强沙尘暴" -> R.drawable.ic_weather_strongsandstorm
        "雷雨冰雹" -> R.drawable.ic_weather_thunderstormandhail
        "浮尘" -> R.drawable.ic_weather_dust
        "扬沙" -> R.drawable.ic_weather_yangsha
        else -> -1
    }
}

fun Weather.getEmoteState(): EmoteState {
    val processedWeather = preprocessWeather()
    return when (processedWeather) {
        "晴" -> EmoteState.WEATHER_SUNNY
        "雾", "雾霾" -> EmoteState.WEATHER_FOG
        "阴" -> EmoteState.WEATHER_OVERCAST
        "多云" -> EmoteState.WEATHER_CLOUDY
        "雨", "小雨", "中雨", "阵雨", "雷阵雨" -> EmoteState.WEATHER_SMALL_RAIN
        "大雨", "暴雨", "大暴雨", "特大暴雨", "冻雨", "雷雨冰雹" -> EmoteState.WEATHER_RAINSTORM
        "雪", "小雪", "中雪", "大雪", "暴雪", "阵雪", "雨夹雪" -> EmoteState.WEATHER_SNOW
        "沙尘暴", "强沙尘暴", "浮尘", "扬沙" -> EmoteState.WEATHER_SANDSTORM
        else -> EmoteState.IDLE
    }
}

fun Weather.preprocessWeather(): String {
    val weatherText = weather
    // 如果包含"转"字，说明是转换天气，取第一个主要天气状态
    if (weatherText.contains("转")) {
        val weatherParts = weatherText.split("转")
        return weatherParts.firstOrNull()?.trim() ?: weatherText
    }
    return weatherText
}