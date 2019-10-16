package com.example.gb28181_videoplatform.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.EditText;

import com.example.gb28181_videoplatform.adapter.TagAdapter;
import com.example.gb28181_videoplatform.widget.flow.FlowTagLayout;
import com.example.gb28181_videoplatform.widget.flow.OnTagSelectListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by 吴迪 on 2019/7/10.
 * 常用工具类
 */
public class MyUtil {

    /**
     *  沉浸式通知栏
     * @param activity 对象
     * @param color 颜色值
     */
    public static void immersiveNotificationBar(Activity activity, int color){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(color));
        }
    }

    /**
     * 设置EditText的属性
     * @param editText 控件对象
     * @param hintText et提示语
     * @param size 字体大小
     */
    public static void setEditText(EditText editText, String hintText, int size, String text){
        SpannableString ss = new SpannableString(hintText);//定义hint的值
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(size,true);//设置字体大小 true表示单位是sp
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.setHint(new SpannedString(ss));
        if (!TextUtils.isEmpty(text)) {
            editText.setText(text);
            editText.setSelection(text.length());
        }
    }

    /**
     * 隐藏系统ui
     * @param decorView 系统view
     */
    public static void hideSystemUI(View decorView) {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * 显示系统ui
     * @param decorView 系统view
     */
    public static void showSystemUI(View decorView) {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    /**
     * 设置View显示动画
     *      1, 0, 0, 0 从右向左显示
     *      0, 0, -1, 0 从上到下显示
     * @param mShowAction 动画
     * @param view 加载动画的view
     */
    public static void showAnimation(Animation mShowAction, View view){
        view.setVisibility(View.VISIBLE);
        view.startAnimation(mShowAction);
    }

    /**
     * 设置View隐藏动画
     *      0, 1, 0, 0 从左向右隐藏
     *      0, 0, 0, -1 从下到上隐藏
     * @param mCloseAction 动画
     * @param view 加载动画的view
     */
    public static void closeAnimation(Animation mCloseAction, final View view) {
        view.startAnimation(mCloseAction);
        mCloseAction.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    /**
     * 获取本机连接网络的IP地址
     * @param context 上下文对象
     * @return wifi地址
     */
    public static String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("111", "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }

    /**
     * 获取本机连接网络的IP地址（手机卡或wifi地址）
     * @param context 上下文对象
     * @return wifi地址
     */
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
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
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }


    /**
     * 将得到的int类型的IP转换为String类型
     * @param ip ip地址
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    /**
     * 获取应用程序版本名称信息
     * @param context 上下文对象
     * @return 程序的版本号
     */
    public static synchronized String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化下拉选择栏并设置选中事件
     * @param tagAdapter 适配器
     * @param flowTagLayout 筛选框
     * @param tagMode 筛选模式
     * @param isStatus 判断是状态还是类型
     * @return 拼接完成的字符串
     */
    public static StringBuilder initFlowLayout(TagAdapter<String> tagAdapter, FlowTagLayout flowTagLayout,
                                               int tagMode, final boolean isStatus){
        final StringBuilder sb = new StringBuilder();
        flowTagLayout.setTagCheckedMode(tagMode);
        flowTagLayout.setAdapter(tagAdapter);
        //设置多选事件，得到选中的每个数据
        flowTagLayout.setOnTagSelectListener(new OnTagSelectListener() {
            @Override
            public void onItemSelect(FlowTagLayout parent, List<Integer> selectedList) {
                if (selectedList != null && selectedList.size() > 0) {
                    sb.delete(0, sb.length());
                    for (int i : selectedList) {
                        if(isStatus){
                            sb.append(parent.getAdapter().getItem(i));
                        }else {
                            sb.append("'");
                            sb.append(parent.getAdapter().getItem(i));
                            sb.append("'");
                            sb.append(",");
                        }
                    }
                }else {
                    sb.delete(0, sb.length());
                }
            }
        });
        return sb;
    }

    /**
     * 下拉选择栏数据填充
     * @param tagAdapter 适配器
     * @param data 数据源
     * @param isStatus 判断是状态还是类型
     */
    public static void initFlowData(TagAdapter<String> tagAdapter, List<String> data, boolean isStatus){
        List<String> dataSource = new ArrayList<>();
        if(isStatus){
            dataSource.add("在线");
            dataSource.add("离线");
        }else {
            dataSource.addAll(data);
        }
        tagAdapter.onlyAddAll(dataSource);
    }

    /**
     * 设置两个view的可见模式
     * @param view1 view1
     * @param view2 View
     * @param isVisibility 可见or不可见
     */
    public static void controlTwoView(View view1, View view2, boolean isVisibility){
        if(isVisibility){
            view1.setVisibility(View.VISIBLE);
            view2.setVisibility(View.VISIBLE);
        }else {
            view1.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);
        }
    }

    /**
     * 动态设置view宽高
     * @param view view对象
     * @param width 宽
     * @param height 高
     */
    public static void setViewParams(View view, int width, int height){
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    /**
     * 判断字符串是否是json结构
     * @param msg 字符串
     * @return 返回
     */
    public static boolean isJson(String msg) {
        try {
            new JSONObject(msg);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    public static Bitmap screenCapture(Activity activity){
        View decorView = activity.getWindow().getDecorView();
        decorView.setDrawingCacheEnabled(true);
        decorView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(decorView.getDrawingCache());
        if (bitmap != null) {
            try {
                // 获取内置SD卡路径
                String sdCardPath = Environment.getExternalStorageDirectory().getPath();
                // 图片文件路径
                String filePath = sdCardPath + File.separator + "screenshot.png";
                File file = new File(filePath);
                FileOutputStream os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
                Log.d("111", "存储完成");
            } catch (Exception e) {
                Log.d("111", e.toString());
            }
            return bitmap;
        }
        return null;
    }

}
