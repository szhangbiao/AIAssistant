package cn.booslink.llm.speech.config

import com.google.gson.annotations.SerializedName

data class AIUIConfig(
    val login: LoginConfig,
    val global: GlobalConfig,
    val interact: InteractConfig,
    val vad: VadConfig,
    val iat: IatConfig,
    @SerializedName("audioparams")
    val audioParams: AudioParamsConfig,
    val ivw: IvwConfig,
    val recorder: RecorderConfig,
    val speech: SpeechConfig,
    val tts: TtsConfig,
    @SerializedName("cbmparams")
    val cbmParams: CbmParamsConfig,
    val header: HeaderConfig,
    val log: LogConfig
)

data class LoginConfig(
    @SerializedName("appid")
    val appId: String,
    val key: String,
    @SerializedName("api_secret")
    val apiSecret: String
)

data class GlobalConfig(
    val scene: String,
    @SerializedName("clean_dialog_history")
    val cleanDialogHistory: String,
    @SerializedName("aiui_ver")
    val aiuiVersion: String
)

data class InteractConfig(
    @SerializedName("interact_timeout")
    val interactTimeout: String,
    @SerializedName("result_timeout")
    val resultTimeout: String
)

data class VadConfig(
    @SerializedName("vad_enable")
    val vadEnable: String,
    @SerializedName("engine_type")
    val engineType: String,
    @SerializedName("res_type")
    val resType: String,
    @SerializedName("res_path")
    val resPath: String,
    @SerializedName("vad_eos")
    val vadEos: String
)

data class IatConfig(
    @SerializedName("sample_rate")
    val sampleRate: String,
    @SerializedName("data_encoding")
    val dataEncoding: String
)

data class AudioParamsConfig(
    @SerializedName("pers_param")
    val persParam: String
)

data class IvwConfig(
    @SerializedName("mic_type")
    val micType: String,
    @SerializedName("res_type")
    val resType: String,
    @SerializedName("res_path")
    val resPath: String
)

data class RecorderConfig(
    @SerializedName("channel_count")
    val channelCount: Int,
    @SerializedName("channel_filter")
    val channelFilter: String
)

data class SpeechConfig(
    @SerializedName("data_source")
    val dataSource: String,
    @SerializedName("wakeup_mode")
    val wakeupMode: String,
    @SerializedName("interact_mode")
    val interactMode: String,
    @SerializedName("work_mode")
    val workMode: String,
    @SerializedName("audio_source")
    val audioSource: Int
)

data class TtsConfig(
    @SerializedName("voice_name")
    val voiceName: String,
    @SerializedName("play_mode")
    val playMode: String
)

data class CbmParamsConfig(
    val nlp: NlpConfig
)

data class NlpConfig(
    val nlp: NlpDetailConfig,
    @SerializedName("sub_scene")
    val subScene: String
)

data class NlpDetailConfig(
    val encoding: String,
    val compress: String,
    val format: String
)

data class HeaderConfig(
    @SerializedName("prot_interact_mode")
    val protInteractMode: String
)

data class LogConfig(
    @SerializedName("debug_log")
    val debugLog: String,
    @SerializedName("save_datalog")
    val saveDatalog: String,
    @SerializedName("datalog_path")
    val datalogPath: String,
    @SerializedName("datalog_size")
    val datalogSize: Int,
    @SerializedName("raw_audio_path")
    val rawAudioPath: String
)
