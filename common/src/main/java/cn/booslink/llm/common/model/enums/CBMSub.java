package cn.booslink.llm.common.model.enums;

/**
 * event	            事件结果（vad事件Bos、Eos，结束交互事件：Silence）
 * iat	                语音识别结果
 * cbm_tidy	            语义规整结果
 * cbm_semantic	        传统语义技能结果
 * cbm_tool_pk	        意图落域结果
 * cbm_knowledge	    知识溯源结果
 * cbm_plugin	        智能体结果
 * nlp	                大模型回复结果
 * tpp	                应用后处理结果
 * tts	                合成结果
 */
public enum CBMSub {
    UNKNOWN(""),
    EVENT("event"),
    IAT("iat"),
    CBM_TIDY("cbm_tidy"),
    CBM_SEMANTIC("cbm_semantic"),
    CBM_TOOL_PK("cbm_tool_pk"),
    CBM_KNOWLEDGE("cbm_knowledge"),
    CBM_RETRIEVAL_CLASSIFY("cbm_retrieval_classify"),
    NLP("nlp"),
    TPP("tpp"),
    TTS("tts");

    private final String sub;

    CBMSub(String sub) {
        this.sub = sub;
    }

    public String getSub() {
        return sub;
    }

    /**
     * 根据字符串值获取对应的枚举
     *
     * @param subValue 字符串值
     * @return 对应的枚举，如果未匹配到返回UNKNOWN
     */
    public static CBMSub fromString(String subValue) {
        if (subValue == null) {
            return UNKNOWN;
        }
        for (CBMSub cbmSub : CBMSub.values()) {
            if (cbmSub.getSub().equals(subValue)) {
                return cbmSub;
            }
        }
        return UNKNOWN;
    }
}
