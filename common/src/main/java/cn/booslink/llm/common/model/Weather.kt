package cn.booslink.llm.common.model

import com.google.gson.annotations.SerializedName

/**
 * Weather data model based on JSON structure
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