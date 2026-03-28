package cn.booslink.llm.common.utils;

import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class HttpEngine {
    private static final String TAG = "HttpEngine";

    /**
     * 兼容 android 4.4 TLS的版本优先选v1.2
     * https://github.com/square/okhttp/issues/6188
     * https://medium.com/tech-quizlet/working-with-tls-1-2-on-android-4-4-and-lower-f4f5205629a
     *
     * @return
     */
    public static SSLContext getSSLContext() {
        boolean tryTls12 = (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22);
        if (tryTls12) {
            try {
                Log.d(TAG, "try to get SSLContext TLSv1.2");
                return SSLContext.getInstance("TLSv1.2");
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "fail to get SSLContext TLSv1.2 : " + e);
            }
        }
        try {
            Log.d(TAG, "try get SSLContext TLS");
            return SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No TLS provider", e);
        }
    }

    /*
     * 创建HttpClient实例
     */
    public OkHttpClient createClient() {
        return createClientBuilder(false, 10 * 1000, 10 * 1000).build();
    }

    public static synchronized OkHttpClient.Builder createClientBuilder(boolean isTrustAll, int connectTimeoutMs, int readTimeoutMs) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = getSSLContext();
            TrustAllManagerImpl trustAllManager = null;
            //Android4.4及以下设备，强制忽略证书校验
            if (Build.VERSION.SDK_INT <= 19) {
                isTrustAll = true;
            }
            if (isTrustAll) {
                trustAllManager = new TrustAllManagerImpl();
                sslContext.init(null, new TrustManager[]{trustAllManager}, null);
            } else {
                sslContext.init(null, null, null);
            }
            sslSocketFactory = new TLSCompatSocketFactory(sslContext.getSocketFactory());
            if (isTrustAll) {
                builder.sslSocketFactory(sslSocketFactory, trustAllManager);
            } else if (Build.VERSION.SDK_INT <= 19) {
                builder.sslSocketFactory(sslSocketFactory);
            }
            if (connectTimeoutMs > 0) {
                builder.connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS);
            }
            if (readTimeoutMs > 0) {
                builder.readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS);
            }
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Throwable t) {
            Log.e(TAG, "createHttpClientBuilder error: " + t);
        }
        return builder;
    }

    public static class TrustAllManagerImpl implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return;
        }


        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**
     * Android4.4及其以下设备启用TLS1.2
     */
    public static class TLSCompatSocketFactory extends SSLSocketFactory {
        private static final String[] TLS_SUPPORT_VERSION;
        private static volatile String[] FINAL_TLS_SUPPORT_VERSION;

        static {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                TLS_SUPPORT_VERSION = new String[]{"TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"};
            } else {
                TLS_SUPPORT_VERSION = new String[]{};
            }
        }

        final SSLSocketFactory delegate;

        public TLSCompatSocketFactory(SSLSocketFactory base) {
            this.delegate = base;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return patch(delegate.createSocket(s, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return patch(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return patch(delegate.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return patch(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return patch(delegate.createSocket(address, port, localAddress, localPort));
        }

        private Socket patch(Socket s) {
            try {
                if (s instanceof SSLSocket && TLS_SUPPORT_VERSION.length > 0) {
                    SSLSocket sslSocket = (SSLSocket) s;
                    checkSupportProtocol(sslSocket);
                    if (FINAL_TLS_SUPPORT_VERSION != null && FINAL_TLS_SUPPORT_VERSION.length > 0) {
                        sslSocket.setEnabledProtocols(FINAL_TLS_SUPPORT_VERSION);
                    }
                }
                printSslSocketProtocols(s);
            } catch (Throwable e) {
                //为保险，加一层保护。
                Log.i(TAG, "setEnabledProtocols: " + e);
            }
            return s;
        }

        private void checkSupportProtocol(SSLSocket sslSocket) {
            if (FINAL_TLS_SUPPORT_VERSION != null) {
                return;
            }
            String[] supportedProtocols = sslSocket.getSupportedProtocols();
            Set<String> supportProtocols = new HashSet<>();
            if (supportedProtocols != null) {
                for (int i = 0; i < TLS_SUPPORT_VERSION.length; i++) {
                    if (isSupportProtocol(TLS_SUPPORT_VERSION[i], supportedProtocols)) {
                        supportProtocols.add(TLS_SUPPORT_VERSION[i]);
                    }
                }
                String[] supportProtocolsArrays = supportProtocols.toArray(new String[0]);
                if (supportProtocolsArrays != null && supportProtocolsArrays.length > 0) {
                    FINAL_TLS_SUPPORT_VERSION = supportProtocolsArrays;
                }
            }
        }

        private boolean isSupportProtocol(String protocol, String[] supportedProtocols) {
            if (supportedProtocols == null || supportedProtocols.length == 0) {
                return false;
            }
            for (int i = 0; i < supportedProtocols.length; i++) {
                if (supportedProtocols[i].equals(protocol)) {
                    return true;
                }
            }
            return false;
        }

        private void printSslSocketProtocols(Socket socket) {
            if (socket == null) {
                return;
            }
            if (!(socket instanceof SSLSocket)) {
                return;
            }
            SSLSocket sslSocket = ((SSLSocket) socket);
            Log.e(TAG, "sslSocket impl = " + sslSocket.getClass());
            printItem(sslSocket.getSupportedProtocols(), "supportProtocol", "不支持任何SSL或TLS协议");
            printItem(sslSocket.getEnabledProtocols(), "enabledProtocol", "没有可用的SSL或TLS协议");
        }

        private void printItem(String[] supportedProtocols, String tag, String tip) {
            try {
                if (supportedProtocols != null) {
                    for (String protocol : supportedProtocols) {
                        Log.e(TAG, tag + ": " + protocol);
                    }
                } else {
                    Log.e(TAG, tip);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
