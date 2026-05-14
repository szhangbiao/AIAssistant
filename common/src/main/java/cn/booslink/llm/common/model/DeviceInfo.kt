package cn.booslink.llm.common.model

import cn.booslink.llm.common.model.enums.Channel

data class DeviceInfo(val isDevMode: Boolean, val isSystemApp: Boolean, val appName: String, val packageName: String, val versionName: String, val versionCode: Int, val channel: Channel) {

    fun isAutoAudioRecord(): Boolean {
        return channel != Channel.VOICE
    }
}
