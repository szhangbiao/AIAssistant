package cn.booslink.llm.downloader.model;

public enum InstallState {
    UNKNOWN("安装失败"),
    SUCCESS("安装成功"),
    USER_RESTRICTED("用户限制安装"),
    USER_CANCELED("用户取消安装"),
    CONFLICT("安装包与现有的包冲突"),
    INCOMPATIBLE("安装包与现有的包不兼容"),
    INVALID("无效的安装包文件"),
    INVALID_PATH("无效的安装包路径"),
    INSUFFICIENT_STORAGE("设备存储空间不足，安装失败"),
    OLDER_SDK("安装包要求的SDK与设备不兼容"),
    ABI_INCOMPATIBLE("安装包要求的CPU架构与设备不兼容"),
    UID_CHANGED("安装包的UID与现有的包不匹配");

    private final String message;

    InstallState(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
