package com.example.gb28181_videoplatform.netty.util;


import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;


import com.example.gb28181_videoplatform.app.Global;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @描述: 自检，打开4G,蓝牙,wifi探测,检查电量
 * @包名: com.fiberhome.police.powerpolice.service.impl
 * @类名: SelfCheckImpl
 * @日期: 2017/4/12
 * @版权: Copyright ® 烽火星空. All right reserved.
 * @作者: fsg
 */
public class SelfCheckImpl {
    Logger mLog= LoggerFactory.getLogger(SelfCheckImpl.class);
    PoliceService mParent;
    private Context mContext;
    private DeviceInfo mDeviceInfo;

    public SelfCheckImpl(PoliceService mParent) {
        this.mParent = mParent;
        mContext=mParent.mContext;
    }

    public DeviceInfo getDeviceInfo(){
        if(mDeviceInfo==null){
            mLog.error("terrible thing happened!");
            throw new NullPointerException("value is null");
        }
        return mDeviceInfo;
    }

    public void setDeviceInfo(){
        mDeviceInfo=new DeviceInfo();
        TelephonyManager manager=(TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mDeviceInfo.setImsi(manager.getSubscriberId()==null ? "":manager.getSubscriberId());
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mDeviceInfo.setEsn(manager.getImei()==null? "863525141005779":manager.getImei());
            if(mDeviceInfo.getEsn().length() != 15){
                mDeviceInfo.setEsn(mDeviceInfo.getImsi());
            }
        }else {
            mDeviceInfo.setEsn(Global.getInstance().getDeviceId());
        }

//        mParent.mSessionId=mDeviceInfo.getEsn();
        //mDeviceInfo.setMac(getMacAddressFromIp());
        PackageManager packageManager=mContext.getPackageManager();
        try {
            mDeviceInfo.setClientVersion(packageManager.getPackageInfo(mContext.getPackageName(),0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mDeviceInfo.setPlatformId(Build.MODEL);
        mDeviceInfo.setOsVersion(Build.VERSION.RELEASE);
        DisplayMetrics displayMetrics=mContext.getResources().getDisplayMetrics();
        mDeviceInfo.setScreenWidth(""+displayMetrics.widthPixels);
        mDeviceInfo.setScreenHeight(""+displayMetrics.heightPixels);
        mDeviceInfo.setDpi(""+displayMetrics.densityDpi);
        mDeviceInfo.setDeviceType("70004");
        mDeviceInfo.setNetwork(getNetworkType());
        mLog.debug(mDeviceInfo.toString());
    }

    public  String getNetworkType(){
        String strNetworkType = "";
        NetworkInfo networkInfo = ((ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
        {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                strNetworkType = "wifi";
            }
            else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String strSubTypeName = networkInfo.getSubtypeName();
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        strNetworkType = "2G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        strNetworkType = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        strNetworkType = "4G";
                        break;
                    default:
                        if ("TD-SCDMA".equalsIgnoreCase(strSubTypeName) || "WCDMA".equalsIgnoreCase(strSubTypeName) || "CDMA2000".equalsIgnoreCase(strSubTypeName)) {
                            strNetworkType = "3G";
                        }
                        else {
                            strNetworkType = strSubTypeName;
                        }
                        break;
                }
            }
        }
        return strNetworkType;
    }

    public String getMacAddressFromIp() {
        String mac_s= "";
        StringBuilder buf = new StringBuilder();
        try {
            byte[] mac;
            NetworkInterface ne=NetworkInterface.getByInetAddress(InetAddress.getByName(getIpAddress(mContext)));
            mac = ne.getHardwareAddress();
            for (byte b : mac) {
                buf.append(String.format("%02X-", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            mac_s = buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mac_s;
    }

    public static String getIpAddress(Context context){
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            // 3/4g网络
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                //  wifi网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
                return ipAddress;
            }  else if (info.getType() == ConnectivityManager.TYPE_ETHERNET){
                // 有限网络
                return getLocalIp();
            }
        }
        return null;
    }

    private static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


    // 获取有限网IP
    private static String getLocalIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {

        }
        return "0.0.0.0";
    }
}
