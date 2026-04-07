package cn.booslink.llm.common.model.enums;

public enum ApkStatus {
    UNKNOWN(-1),
    DOWNLOAD_PADDING(0),
    DOWNLOAD_PROGRESS(1),
    DOWNLOAD_PAUSE(2),
    DOWNLOAD_PAUSE_WITH_ERROR(3),
    DOWNLOAD_FAIL(4),
    INSTALL_PADDING(5),
    INSTALL_GOING(6),
    INSTALL_SUCCESS(7),
    INSTALL_FAIL(8),
    INSTALL_RE_PADDING(9);

    private final int status;

    ApkStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
