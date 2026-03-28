package cn.booslink.llm.common.model;

import android.content.Context;
import android.os.Build;

import com.google.gson.annotations.SerializedName;

import cn.booslink.util.DeviceUtils;

public class Device {
    @SerializedName("eth_mac")
    public String ethMac;
    @SerializedName("wifi_mac")
    public String wifiMac;
    @SerializedName("android_id")
    public String androidId;
    public String sn;
    @SerializedName("cpu_id")
    public String cpuId;
    public String model;
    public String channel;
    public String version;
    @SerializedName("device_id")
    public String deviceId;
    // public long ts;
    // public String qid;

    public static Device of(Context appContext, String channel, String version) {
        Device device = new Device();
        device.ethMac = DeviceUtils.getEthMac();
        device.wifiMac = DeviceUtils.getWifiMac(appContext);
        device.androidId = DeviceUtils.getAndroidId(appContext);
        device.sn = DeviceUtils.getSn();
        device.cpuId = DeviceUtils.getChipIDHex();
        device.model = Build.MODEL;
        device.channel = channel;
        device.version = version;
        return device;
    }
}
