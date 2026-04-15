package cn.booslink.llm.common.model

import cn.booslink.llm.common.model.enums.QueryState

data class VoiceQuery(val query: String?, val state: QueryState) {
    companion object {
        fun startup() = VoiceQuery("您好，我是Bobo！", QueryState.IDLE)
    }
}
