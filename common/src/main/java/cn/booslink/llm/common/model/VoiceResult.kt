package cn.booslink.llm.common.model

data class VoiceResult(val handled: Boolean,val ignoreNlpResponse: Boolean, val responseText: String?) {
    companion object {
        fun success(responseText: String) = VoiceResult(handled = true, ignoreNlpResponse = false, responseText = responseText)
        fun failure() = VoiceResult(handled = false, ignoreNlpResponse = false, responseText = null)
        fun progress() = VoiceResult(true, ignoreNlpResponse = false, responseText = "正在为你处理")
        fun ignore() = VoiceResult(true, ignoreNlpResponse = true, responseText = null)
    }
}
