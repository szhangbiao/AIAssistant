package cn.booslink.llm.downloader.utils;

import com.liulishuo.okdownload.core.exception.DownloadSecurityException;
import com.liulishuo.okdownload.core.exception.FileBusyAfterRunException;
import com.liulishuo.okdownload.core.exception.InterruptException;
import com.liulishuo.okdownload.core.exception.NetworkPolicyException;
import com.liulishuo.okdownload.core.exception.PreAllocateException;
import com.liulishuo.okdownload.core.exception.ResumeFailedException;
import com.liulishuo.okdownload.core.exception.RetryException;
import com.liulishuo.okdownload.core.exception.ServerCanceledException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class DownloadErrorUtils {
    public static String humanReadableError(Exception exception) {
        if (exception instanceof ServerCanceledException) {
            return "下载出错，请联系管理员";
        }
        if (exception instanceof DownloadSecurityException) {
            return "下载校验出错，请稍后重试";
        }
        if (exception instanceof FileBusyAfterRunException) {
            return "文件读写繁忙，请稍后重试";
        }
        if (exception instanceof InterruptException) {
            return "下载中断，请稍后重试";
        }
        if (exception instanceof NetworkPolicyException) {
            return "当前只允许Wifi下载";
        }
        if (exception instanceof PreAllocateException) {
            return "申请存储空间出错，请稍后重试";
        }
        if (exception instanceof ResumeFailedException) {
            return "恢复下载失败，请稍后重试";
        }
        if (exception instanceof RetryException) {
            return "下载重试出错，请稍后重试";
        }
        if (exception instanceof SocketTimeoutException || exception instanceof UnknownHostException) {
            return "网络出错，请稍后重试";
        }
        if (exception instanceof SocketException) {
            return "网络出错，请稍后重试";
        }
        return "未知错误";
    }
}
