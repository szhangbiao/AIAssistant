package cn.booslink.llm.common.model

import cn.booslink.llm.common.model.enums.CBMSub
import com.google.gson.annotations.SerializedName

data class EventInfo(val data: List<InfoData>?) {
    fun getSub(): CBMSub? {
        return data?.firstOrNull()?.params?.sub
    }

    fun getCntId(): String? {
        return data?.firstOrNull()?.content?.firstOrNull()?.cntId
    }
}

data class InfoData(val content: List<DataContent>, val params: DataParams)

data class DataContent(@SerializedName("cnt_id") val cntId: String, val dte: String)

data class DataParams(val sub: CBMSub)