package cn.booslink.llm.downloader.listener;

import static cn.booslink.llm.downloader.AppManagerImpl.TASK_TAG_REPADDING;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.exception.ServerCanceledException;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.model.enums.ApkStatus;
import cn.booslink.llm.common.utils.FileUtils;
import cn.booslink.llm.downloader.utils.DownloadErrorUtils;
import timber.log.Timber;

public class SimpleDownloadListener extends DownloadListener4WithSpeed {

    private static final String TAG = "SimpleDownload";
    private final ApkDownload mDownloadItem;
    private final OnApkDownloadListener mOnApkDownloadListener;
    private long totalLength;
    private String readableTotalLength;

    public SimpleDownloadListener(ApkDownload downloadItem, OnApkDownloadListener onApkDownloadListener) {
        this.mDownloadItem = downloadItem;
        this.mOnApkDownloadListener = onApkDownloadListener;
    }

    @Override
    public void taskStart(@NonNull DownloadTask task) {
        Timber.tag(TAG).d("Name:" + mDownloadItem.getName() + ">>【1、taskStart】" + task.getId() + " " + task.getFilename());
        mDownloadItem.setStatus(ApkStatus.DOWNLOAD_PROGRESS);
        mDownloadItem.setSpeed("0 KB/S");
        if (mOnApkDownloadListener != null) {
            String apkPath = task.getFile() != null ? task.getFile().getAbsolutePath() : "";
            mOnApkDownloadListener.onDownloadUpdate(apkPath, mDownloadItem);
        }
    }

    @Override
    public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info, boolean fromBreakpoint, @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
        totalLength = info.getTotalLength();
        readableTotalLength = Util.humanReadableBytes(totalLength, false);
        String readableOffset = Util.humanReadableBytes(info.getTotalOffset(), false);
        String progressStatus = readableOffset + "/" + readableTotalLength;
        float progress = totalLength != 0 ? (info.getTotalOffset() * 1.0f / totalLength) * 100f : 0f;
        Timber.tag(TAG).d("Name:" + mDownloadItem.getName() + ">>【2、infoReady】[%s]， 当前进度：%s,总长度：%s", progressStatus, progress, readableTotalLength);
        if (mDownloadItem.getStatus() != ApkStatus.DOWNLOAD_PROGRESS) return;
        // mDownloadItem.progress((int) progress, String.format("%s, %s", "0KB/S", progressStatus));
        mDownloadItem.progress((int) progress, "0 KB/S");
        if (mOnApkDownloadListener != null) {
            String apkPath = task.getFile() != null ? task.getFile().getAbsolutePath() : "";
            mOnApkDownloadListener.onDownloadUpdate(apkPath, mDownloadItem);
        }
    }

    @Override
    public void connectStart(@NonNull DownloadTask task, int blockIndex, @NonNull Map<String, List<String>> requestHeaderFields) {
        Timber.tag(TAG).d("Name:" + mDownloadItem.getName() + ">>【3、connectStart】%s", blockIndex);
    }

    @Override
    public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode, @NonNull Map<String, List<String>> responseHeaderFields) {
        Timber.tag(TAG).d("Name:" + mDownloadItem.getName() + ">>【4、connectEnd】" + blockIndex + "，" + responseCode);
    }

    @Override
    public void progressBlock(@NonNull DownloadTask task, int blockIndex, long currentBlockOffset, @NonNull SpeedCalculator blockSpeed) {
        Timber.tag(TAG).d("Name:" + mDownloadItem.getName() + ">>【5、progressBlock】" + blockIndex + "，" + currentBlockOffset);
    }

    @Override
    public void progress(@NonNull DownloadTask task, long currentOffset, @NonNull SpeedCalculator taskSpeed) {
        String readableOffset = Util.humanReadableBytes(currentOffset, false);
        String progressStatus = readableOffset + "/" + readableTotalLength;
        String humanReadableSpeed = taskSpeed.speed();
        float progress = totalLength != 0 ? (currentOffset * 1.0f / totalLength) * 100f : 0f;
        Timber.tag(TAG).d("Name:" + mDownloadItem.getName() + ">>【6、progress】[" + progressStatus + "]，速度：" + humanReadableSpeed + "，进度：" + progress + "%");
        // Download listener回调是无序的，会发生task end之后继续回调progress的情况
        String speedStrUppercase = humanReadableSpeed.toUpperCase();
        speedStrUppercase = "0 B/S".equals(speedStrUppercase) ? "0 KB/S" : speedStrUppercase;
        if (mDownloadItem.getStatus() != ApkStatus.DOWNLOAD_PROGRESS) {
            mDownloadItem.progressOnly((int) progress, speedStrUppercase);
        } else {
            mDownloadItem.progress((int) progress, speedStrUppercase);
        }
        if (mOnApkDownloadListener != null) {
            String apkPath = task.getFile() != null ? task.getFile().getAbsolutePath() : "";
            mOnApkDownloadListener.onDownloadUpdate(apkPath, mDownloadItem);
        }
    }

    @Override
    public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info, @NonNull SpeedCalculator blockSpeed) {
        long blockContentLength = info.getContentLength();
        String readableTotalLength = Util.humanReadableBytes(blockContentLength, false);
        Timber.tag(TAG).d("Name:" + mDownloadItem.getName() + ">>【7、blockEnd】index:%s, length:%s", blockIndex, readableTotalLength);
    }

    @Override
    public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull SpeedCalculator taskSpeed) {
        switch (cause) {
            case COMPLETED:
                validDownloadFile(task);
                break;
            case CANCELED:
                if (task.getTag().equals(TASK_TAG_REPADDING)) {
                    mDownloadItem.setStatus(ApkStatus.DOWNLOAD_PADDING);
                } else if (mDownloadItem.isDownloadComplete()) {
                    validDownloadFile(task);
                    break;
                }
                if (mOnApkDownloadListener != null) {
                    String apkPath = task.getFile() != null ? task.getFile().getAbsolutePath() : "";
                    mOnApkDownloadListener.onDownloadUpdate(apkPath, mDownloadItem);
                }
                break;
            case ERROR:
                mDownloadItem.setSpeed(DownloadErrorUtils.humanReadableError(realCause));
                if (realCause instanceof IOException && realCause.getMessage() != null && realCause.getMessage().contains("No space left on device")) {
                    mDownloadItem.downloadPauseWithNoSpace();
                    if (mOnApkDownloadListener != null) {
                        mOnApkDownloadListener.onDownloadUpdate(null, mDownloadItem);
                    }
                } else if (realCause instanceof ServerCanceledException) {
                    mDownloadItem.downloadFail();
                    if (mOnApkDownloadListener != null) {
                        mOnApkDownloadListener.onDownloadFailed(mDownloadItem);
                    }
                } else {
                    mDownloadItem.setStatus(ApkStatus.DOWNLOAD_PAUSE_WITH_ERROR);
                    if (mOnApkDownloadListener != null) {
                        String apkPath = task.getFile() != null ? task.getFile().getAbsolutePath() : "";
                        mOnApkDownloadListener.onDownloadUpdate(apkPath, mDownloadItem);
                    }
                }
                break;
            case PRE_ALLOCATE_FAILED:
                mDownloadItem.downloadPauseWithNoSpace();
                if (mOnApkDownloadListener != null) {
                    mOnApkDownloadListener.onDownloadUpdate(null, mDownloadItem);
                }
                break;
            case FILE_BUSY:
            case SAME_TASK_BUSY:
                // TODO 先保留
                break;
            default:
                Timber.tag(TAG).w("Don't support %s", cause);
        }
        if (realCause != null) {
            Timber.tag(TAG).d("Name:" + mDownloadItem.getName() + ">>【8、taskEnd】" + cause.name() + "：" + realCause.getClass().getName());
        } else {
            Timber.tag(TAG).d("Name:" + mDownloadItem.getName() + ">>【8、taskEnd】" + cause.name() + "：" + "无异常");
        }
    }

    private void validDownloadFile(DownloadTask task) {
        if (TextUtils.isEmpty(task.getUrl()) || TextUtils.isEmpty(mDownloadItem.getMd5Hash())) {
            mDownloadItem.downloadFail();
            Timber.tag(TAG).d("文件下载Url为空或者MD5配置错误");
            if (mOnApkDownloadListener != null) {
                mOnApkDownloadListener.onDownloadFailed(mDownloadItem);
            }
            return;
        }
        String filePath = task.getParentFile().getAbsolutePath() + File.separator + task.getFilename();
        String fileMD5 = FileUtils.getFileMD5(filePath);
        Timber.tag(TAG).d("文件MD5:%s", fileMD5);
        if (mDownloadItem.getMd5Hash().equalsIgnoreCase(fileMD5)) {
            mDownloadItem.setStatus(ApkStatus.INSTALL_PADDING);
            if (mOnApkDownloadListener != null) {
                String apkPath = task.getFile() != null ? task.getFile().getAbsolutePath() : "";
                mOnApkDownloadListener.onDownloadUpdate(apkPath, mDownloadItem);
            }
        } else {
            if (mDownloadItem.getRetryCount() >= 1) {
                mDownloadItem.retryFail();
                if (task.getFile() != null) {
                    FileUtils.deleteFile(task.getFile());
                }
                if (mOnApkDownloadListener != null) {
                    mOnApkDownloadListener.onDownloadFailed(mDownloadItem);
                }
            } else {
                mDownloadItem.retry();
                mDownloadItem.setRetryCount(mDownloadItem.getRetryCount() + 1);
                if (task.getFile() != null) {
                    FileUtils.deleteFile(task.getFile());
                }
                Timber.tag(TAG).d("文件校验失败，重试次数:%s", mDownloadItem.getRetryCount());
                if (mOnApkDownloadListener != null) {
                    mOnApkDownloadListener.onRetryDownload(mDownloadItem);
                }
            }
        }
    }
}
