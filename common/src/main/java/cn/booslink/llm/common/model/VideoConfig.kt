package cn.booslink.llm.common.model

import cn.booslink.llm.common.model.enums.VideoTag

data class VideoConfig(val status: Int?, val info: DataInfo?, val tag: VideoTag?)

data class DataInfo(val id: String?, val name: String?)

