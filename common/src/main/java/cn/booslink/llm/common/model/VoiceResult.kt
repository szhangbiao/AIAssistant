package cn.booslink.llm.common.model

data class VoiceResult(val handled: Boolean = true, val ignoreNlpResponse: Boolean = false, val responseText: String? = null) {
    companion object {
        fun success(responseText: String) = VoiceResult(responseText = responseText)
        fun failure() = VoiceResult(handled = false)
        fun progress() = VoiceResult(responseText = "正在为你处理")
        fun ignore() = VoiceResult(ignoreNlpResponse = true)
    }
}
