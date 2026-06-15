package cn.booslink.llm.common.model;

import java.util.List;

public class GuideData {
    private String version;
    private List<GuideItem> data;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<GuideItem> getData() {
        return data;
    }

    public void setData(List<GuideItem> data) {
        this.data = data;
    }
}
