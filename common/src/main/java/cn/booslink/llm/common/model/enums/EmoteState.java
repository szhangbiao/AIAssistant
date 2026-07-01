package cn.booslink.llm.common.model.enums;

public enum EmoteState {
    IDLE("pag_hello.pag"),
    NORMAL("pag_wink.pag"),
    THINKING("pag_thinking.pag"),
    LAUGHING("pag_laughing.pag"),
    CRYING("pag_crying.pag"),
    WEATHER_SUNNY("pag_sunny.pag"),
    WEATHER_CLOUDY("pag_cloudy.pag"),
    WEATHER_FOG("pag_fog.pag"),
    WEATHER_OVERCAST("pag_overcast.pag"),
    WEATHER_RAINSTORM("pag_rain_storm.pag"),
    WEATHER_SANDSTORM("pag_sand_storm.pag"),
    WEATHER_SMALL_RAIN("pag_small_rain.pag"),
    WEATHER_SNOW("pag_snow.pag");

    private final String fileKey;

    EmoteState(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getFileKey() {
        return fileKey;
    }
}
