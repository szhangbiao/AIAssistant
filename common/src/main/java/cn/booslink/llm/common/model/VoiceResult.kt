package cn.booslink.llm.common.model

data class VoiceResult(val handled: Boolean, val responseText: String?) {
    companion object {
        fun success(responseText: String) = VoiceResult(true, responseText)
        fun failure() = VoiceResult(false, null)
        fun progress() = VoiceResult(true, "正在为你处理")
    }
}
