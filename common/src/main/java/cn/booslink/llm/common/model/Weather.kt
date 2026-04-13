package cn.booslink.llm.common.model

import com.google.gson.annotations.SerializedName

/**
 * {
 * 	"airData": 28,
 * 	"city": "苏州市",
 * 	"date": "2026-04-13",
 * 	"date_for_voice": "今天",
 * 	"dateLong": 1776009600,
 * 	"extra": "",
 * 	"humidity": "83%",
 * 	"img": "http://cdn9002.iflyos.cn/osweathericon/02.png",
 * 	"lastUpdateTime": "2026-04-13 15:00:08",
 * 	"pm25": "14",
 * 	"precipitation": "0",
 * 	"sunRise": "2026-04-13 05:32:00",
 * 	"sunSet": "2026-04-13 18:24:00",
 * 	"temp": 17,
 * 	"tempHigh": "19℃",
 * 	"tempLow": "14℃",
 * 	"tempRange": "14℃ ~ 19℃",
 * 	"tempReal": "16℃",
 * 	"visibility": "",
 * 	"warning": "",
 * 	"weather": "阴",
 * 	"weatherDescription": "温度适宜。",
 * 	"weatherDescription3": "14℃到20℃，风不大，温度适宜。",
 * 	"weatherDescription7": "13℃到23℃，16号有雨，风不大，温度适宜。",
 * 	"weatherType": 2,
 * 	"week": "周一",
 * 	"wind": "东北风3-4级",
 * 	"windLevel": 0
 * }
 */
data class Weather(
    val airData: Int = 0,
    val city: String = "",
    val date: String = "",
    val dateLong: Long = 0,
    @SerializedName("date_for_voice")
    val dateForVoice: String = "",
    val extra: String = "",
    val humidity: String = "",
    val img: String = "",
    @SerializedName("lastUpdateTime")
    val lastUpdateTime: String = "",
    @SerializedName("pm25")
    val pm25: String = "",
    val precipitation: String = "",
    @SerializedName("sunRise")
    val sunRise: String = "",
    @SerializedName("sunSet")
    val sunSet: String = "",
    val temp: Int = 0,
    @SerializedName("tempHigh")
    val tempHigh: String = "",
    @SerializedName("tempLow")
    val tempLow: String = "",
    @SerializedName("tempRange")
    val tempRange: String = "",
    @SerializedName("tempReal")
    val tempReal: String = "",
    val visibility: String = "",
    val warning: String = "",
    val weather: String = "",
    @SerializedName("weatherDescription")
    val weatherDescription: String = "",
    @SerializedName("weatherDescription3")
    val weatherDescription3: String = "",
    @SerializedName("weatherDescription7")
    val weatherDescription7: String = "",
    @SerializedName("weatherType")
    val weatherType: Int = 0,
    val week: String = "",
    val wind: String = "",
    @SerializedName("windLevel")
    val windLevel: Int = 0
)

data class WeatherUI(val current: Weather?, val day1: Weather?, val day2: Weather?, val day3: Weather?, val day4: Weather?) {
    companion object {
        fun fromWeatherList(weatherList: List<Weather>): WeatherUI {
            val current = weatherList.firstOrNull()
            val day1 = weatherList.getOrNull(2)
            val day2 = weatherList.getOrNull(3)
            val day3 = weatherList.getOrNull(4)
            val day4 = weatherList.getOrNull(5)
            return WeatherUI(current, day1, day2, day3, day4)
        }
    }
}