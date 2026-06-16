package cn.booslink.llm.common.model;

import android.content.Context;
import android.os.Build;

import cn.booslink.util.DeviceUtils;

public class Device {
    public String model;
    public String cpuId;
    public String wifiMac;
    public String serialNo;
    private String fingerPrint;

    public static Device of(Context appContext) {
        Device device = new Device();
        device.wifiMac = DeviceUtils.getWifiMac(appContext);
        device.serialNo = DeviceUtils.getSn();//  "1024768003"; H2设备
        device.cpuId = DeviceUtils.getChipIDHex();
        device.model = Build.MODEL;
        device.fingerPrint = Build.FINGERPRINT;
        return device;
    }
}
