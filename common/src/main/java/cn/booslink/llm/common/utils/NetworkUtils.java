package cn.booslink.llm.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.WorkerThread;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class NetworkUtils {

    public static final String TAG = "NetworkUtils";
    public static final int PING_TIME_OUT = 300;

    public static boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // TODO permission ACCESS_NETWORK_STATE
        NetworkInfo localNetworkInfo = manager.getActiveNetworkInfo();
        return localNetworkInfo != null && localNetworkInfo.isConnected();
    }

    @WorkerThread
    public static String getLocalIp() throws IOException {
        Enumeration<NetworkInterface> infoEnumeration = NetworkInterface.getNetworkInterfaces();
        while (infoEnumeration.hasMoreElements()) {
            NetworkInterface networkInterface = infoEnumeration.nextElement();
            Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
            while (inetAddressEnumeration.hasMoreElements()) {
                InetAddress inetAddress = inetAddressEnumeration.nextElement();
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                    String hostAddress = inetAddress.getHostAddress();
                    if (hostAddress != null && !hostAddress.isEmpty()) {
                        return hostAddress;
                    }
                }
            }
        }
        return "";
    }

    public static String getEarthMac() {
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : networkInterfaces) {
                if (nif.getName().equalsIgnoreCase("eth0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) return "";
                    StringBuilder sb = new StringBuilder();
                    for (byte b : macBytes) {
                        sb.append(String.format("%02X:", b));
                    }
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    return sb.toString();
                }
            }
        } catch (SocketException e) {
            Timber.tag(TAG).e(e, "getEarthMac fail!");
        }
        return "00:";
    }

    public static String getWifiMac(Context context) {
        String wifiMac;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            wifiMac = getMacDefault(context);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            wifiMac = getMacAddress();
        } else {
            wifiMac = getMacFromHardware();
        }
        return wifiMac;
    }

    /**
     * Android 6.0 之前（不包括6.0）获取mac地址
     * 必须的权限 <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
     *
     * @param context * @return
     */
    private static String getMacDefault(Context context) {
        String wifiMac = "";
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = null;
        try {
            info = manager.getConnectionInfo();
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "query wifi info fail!");
        }
        if (info == null) {
            return null;
        }
        wifiMac = info.getMacAddress();
        if (!TextUtils.isEmpty(wifiMac)) {
            wifiMac = wifiMac.toUpperCase(Locale.ENGLISH);
        }
        return wifiMac;
    }

    /**
     * Android 6.0-Android 7.0 获取mac地址
     */
    private static String getMacAddress() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat/sys/class/net/wlan0/address");
            InputStreamReader input = new InputStreamReader(pp.getInputStream());
            LineNumberReader reader = new LineNumberReader(input);
            while (null != str) {
                str = reader.readLine();
                if (str != null) {
                    macSerial = str.trim();
                    break;
                }
            }
        } catch (IOException e) {
            Timber.tag(TAG).e(e, "getMacAddress fail!");
        }
        return macSerial;
    }

    /**
     * Android 7.0之后获取Mac地址
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET"></uses-permission>
     */
    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : networkInterfaces) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) {
                    continue;
                }
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) return "";
                StringBuilder sb = new StringBuilder();
                for (Byte b : macBytes) {
                    sb.append(String.format("%02X:", b));
                }
                if (!TextUtils.isEmpty(sb)) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "getMacFromHardware fail!");
        }
        return "02:00:00:00:00:00";
    }

    @WorkerThread
    public static boolean pingIpAddress(String ipAndPort) {
        String ipHost;
        int port;
        if (ipAndPort.contains(":")) {
            ipHost = ipAndPort.split(":")[0];
            try {
                port = Integer.parseInt(ipAndPort.split(":")[1]);
            } catch (NumberFormatException ignored) {
                port = 80;
            }
        } else {
            ipHost = ipAndPort;
            port = 80;
        }
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ipHost, port), PING_TIME_OUT);
            Timber.tag(TAG).i("Ping to " + ipAndPort + " not fail");
            socket.close();
            return true;
        } catch (IOException e) {
            Timber.tag(TAG).e(e, "Ping to " + ipAndPort + " failed");
            return false;
        }
    }

    @WorkerThread
    public static boolean pingUrlHost(String url) {
        Timber.tag(TAG).i("Starting quick ping for %s", url);
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setRequestProperty("User-Agent", "test");
            urlConnection.setConnectTimeout(PING_TIME_OUT);
            urlConnection.setReadTimeout(PING_TIME_OUT);
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            Timber.tag(TAG).i("Ping response code for %s was %d", url, responseCode);
            urlConnection.disconnect();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            Timber.tag(TAG).d("Ping to %s failed", url);
            return false;
        }
    }
}
