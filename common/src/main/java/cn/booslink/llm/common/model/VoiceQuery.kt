package cn.booslink.llm.common.model

import cn.booslink.llm.common.model.enums.QueryState

class VoiceQuery(val query: String?, val state: QueryState) {

    companion object {
        fun startup() = VoiceQuery("您好，我是Bobo！", QueryState.IDLE)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VoiceQuery) return false

        return query == other.query && state == other.state
    }

    override fun hashCode(): Int {
        return 31 * (query?.hashCode() ?: 0) + state.hashCode()
    }

    override fun toString(): String {
        return "VoiceQuery(query=$query, state=$state)"
    }
}
