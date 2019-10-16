package com.example.gb28181_videoplatform.http;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.example.gb28181_videoplatform.bean.DeviceBean;
import com.example.gb28181_videoplatform.bean.DeviceType;
import com.example.gb28181_videoplatform.util.ConstantConfig;
import com.example.gb28181_videoplatform.util.MyUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by 吴迪 on 2019/8/7.
 * OkHttp管理类
 */
public class OkHttpManager {
    private static final String TAG = "OkHttpManager";

    private static OkHttpManager mInstance;
    private OkHttpClient mHttpClient;
    private Gson gson;

    //设备类型
    public static final int DEVICE_TYPE_IPC = 101;

    public static final int DEVICE_TYPE_SMART = 102;

    public static final int DEVICE_TYPE_NVR = 103;

    //设备列表
    public static final int DEVICE_LIST_WHAT = 111;

    //更新NVR
    public static final int NVR_UPDATE_WHAT = 121;

    //请求接口失败
    public static final int DEVICE_REQUEST_ERROR = 404;

    private String typeUrl;

    private String listUrl;

    private String updateUrl;

    private OkHttpManager() {
        mHttpClient = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS).build();
        gson = new Gson();
        //初始化url地址
        if (!TextUtils.isEmpty(SPUtils.getInstance().getString("webPort"))) {
            typeUrl = "http://" + SPUtils.getInstance().getString("webIp") + ":" +
                    SPUtils.getInstance().getString("webPort") + "/appIntelligentDevice/selectAppTypeInfo";
            listUrl = "http://" + SPUtils.getInstance().getString("webIp") + ":" +
                    SPUtils.getInstance().getString("webPort") + "/appIntelligentDevice/getAppDeviceInfo";
            updateUrl = "http://" + SPUtils.getInstance().getString("webIp") + ":" +
                    SPUtils.getInstance().getString("webPort") + "/intelligentDeviceManage/updateNVRDevice";

        }else {
            typeUrl = "http://" + SPUtils.getInstance().getString("webIp") + "/appIntelligentDevice/selectAppTypeInfo";
            listUrl = "http://" + SPUtils.getInstance().getString("webIp") + "/appIntelligentDevice/getAppDeviceInfo";
            updateUrl = "http://" + SPUtils.getInstance().getString("webIp") + "/intelligentDeviceManage/updateNVRDevice";
        }
    }

    /**
     * 单例
     */
    public static OkHttpManager getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpManager.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取设备类型列表
     * @param deviceType vp页码
     */
    public void getDeviceType(final Handler handler, final int deviceType){
        FormBody formBody = new FormBody
                .Builder()
                .add("dateType", String.valueOf(deviceType))
                .build();
        Request request = new Request.Builder()
                .url(typeUrl)
                .post(formBody)
                .build();
        Call call = mHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) {
                String responseMsg = null;
                try {
                    responseMsg = response.body().string();
                    if(MyUtil.isJson(responseMsg)){
                        Message msg = handler.obtainMessage();
                        DeviceType deviceTypeBean = gson.fromJson(responseMsg, DeviceType.class);
                        List<DeviceType.DataBean.PtzTypeListBean> list = deviceTypeBean.getData().getPtzTypeList();
                        if(list != null){
                            List<String> deviceList = new ArrayList<>();
                            for (int i = 0; i < list.size(); i++) {
                                deviceList.add(list.get(i).getDateValue());
                            }
                            if(deviceType == 1){
                                msg.what = DEVICE_TYPE_IPC;
                                msg.obj = deviceList;
                            }else if(deviceType == 2){
                                msg.what = DEVICE_TYPE_SMART;
                                msg.obj = deviceList;
                            }else {
                                msg.what = DEVICE_TYPE_NVR;
                                msg.obj = deviceList;
                            }
                            handler.sendMessage(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                Log.e(TAG, responseMsg);
            }
        });
    }

    /**
     * 获取指定的设备列表
     * @param status 在线状态 判断三种状态
     * @param accessType vp页码 判断是否是全部页面
     * @param ptzType 类型复选框 拼接字符串截掉最后一位
     * @param pageNo 请求页码
     * @param pageSize 请求条数
     */
    public void getDeviceList(final Handler handler, String status, String accessType, String ptzType, int pageNo, int pageSize) {
        Log.e(TAG, "getDeviceList: "+listUrl );
        ptzType = ptzType.equals("") ? "" : ptzType;
        if(!ptzType.equals("")){
            ptzType = ptzType.substring(0, ptzType.length() - 1);
        }
        if(!status.equals("")){
            status = status.equals("在线") ? ConstantConfig.DEVICE_ONLINE : ConstantConfig.DEVICE_OFFLINE;
        }
        FormBody formBody = new FormBody
                .Builder()
                .add("status", status)
                .add("accessType", accessType)
                .add("ptzType", ptzType)
                .add("pageNo",String.valueOf(pageNo))
                .add("pageSize", String.valueOf(pageSize))
                .build();
        Request request = new Request.Builder()
                .url(listUrl)
                .post(formBody)
                .build();
        Call call = mHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg = handler.obtainMessage();
                msg.what = DEVICE_REQUEST_ERROR;
                handler.sendMessage(msg);
                Log.e("onFailure", e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) {
                String responseMsg = null;
                try {
                    responseMsg = response.body().string();
                    if(MyUtil.isJson(responseMsg)){
                        DeviceBean deviceBean = gson.fromJson(responseMsg, DeviceBean.class);
                        Message msg = handler.obtainMessage();
                        msg.what = DEVICE_LIST_WHAT;
                        if(deviceBean.getData() != null){
                            msg.obj = deviceBean.getData().getAppDeviceInfo();
                            handler.sendMessage(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onResponse: "+e.getMessage() );
                }
                Log.e(TAG, responseMsg);
            }
        });
    }

    /**
     * 获取搜索结果下的设备列表
     * @param deviceName 搜索条件
     * @param status 在线状态
     * @param accessType vp页码
     * @param ptzType 类型复选框
     * @param pageNo 请求页码
     * @param pageSize 请求条数
     */
    public void getSearchDevice(final Handler handler, String deviceName, String status, String accessType,
                                String ptzType, int pageNo, int pageSize) {
        ptzType = ptzType.equals("") ? "" : ptzType;
        if(!ptzType.equals("")){
            ptzType = ptzType.substring(0, ptzType.length() - 1);
        }
        if(!status.equals("")){
            status = status.equals("在线") ? ConstantConfig.DEVICE_ONLINE : ConstantConfig.DEVICE_OFFLINE;
        }
        Log.e(TAG, "getSearchDevice: "+deviceName+" === "+status+" === "+accessType+" === "+ptzType);
        FormBody formBody = new FormBody
                .Builder()
                .add("name", deviceName)
                .add("status", status)
                .add("accessType", accessType)
                .add("ptzType", ptzType)
                .add("pageNo",String.valueOf(pageNo))
                .add("pageSize", String.valueOf(pageSize))
                .build();
        Request request = new Request.Builder()
                .url(listUrl)
                .post(formBody)
                .build();
        Call call = mHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg = handler.obtainMessage();
                msg.what = DEVICE_REQUEST_ERROR;
                handler.sendMessage(msg);
                Log.e("onFailure", e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) {
                String responseMsg = null;
                try {
                    responseMsg = response.body().string();
                    if(MyUtil.isJson(responseMsg)){
                        DeviceBean deviceBean = gson.fromJson(responseMsg, DeviceBean.class);
                        Message msg = handler.obtainMessage();
                        msg.what = DEVICE_LIST_WHAT;
                        if(deviceBean.getData() != null){
                            msg.obj = deviceBean.getData().getAppDeviceInfo();
                            handler.sendMessage(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, responseMsg);
            }
        });
    }

    /**
     * 获取NVR下的设备列表
     * @param deviceId NVR设备国标ID
     * @param status 在线状态
     * @param accessType vp页码
     * @param ptzType 类型复选框
     * @param pageNo 请求页码
     * @param pageSize 请求条数
     */
    public void getNVRDeviceList(final Handler handler, String deviceId, String status, String accessType,
                                 String ptzType, int pageNo, int pageSize) {
        ptzType = ptzType.equals("") ? "" : ptzType;
        if(!ptzType.equals("")){
            ptzType = ptzType.substring(0, ptzType.length() - 1);
        }
        if(!status.equals("")){
            status = status.equals("在线") ? ConstantConfig.DEVICE_ONLINE : ConstantConfig.DEVICE_OFFLINE;
        }
        FormBody formBody = new FormBody
                .Builder()
                .add("deviceId", deviceId)
                .add("status", status)
                .add("accessType", accessType)
                .add("ptzType", ptzType)
                .add("pageNo",String.valueOf(pageNo))
                .add("pageSize", String.valueOf(pageSize))
                .build();
        Request request = new Request.Builder()
                .url(listUrl)
                .post(formBody)
                .build();
        Call call = mHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg = handler.obtainMessage();
                msg.what = DEVICE_REQUEST_ERROR;
                handler.sendMessage(msg);
                Log.e("onFailure", e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) {
                String responseMsg = null;
                try {
                    responseMsg = response.body().string();
                    if(MyUtil.isJson(responseMsg)){
                        DeviceBean deviceBean = gson.fromJson(responseMsg, DeviceBean.class);
                        Message msg = handler.obtainMessage();
                        msg.what = DEVICE_LIST_WHAT;
                        if(deviceBean.getData() != null){
                            msg.obj = deviceBean.getData().getAppDeviceInfo();
                            handler.sendMessage(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, responseMsg);
            }
        });
    }

    /**
     * 更新NVR下的设备列表
     * @param deviceId NVR设备国标ID
     */
    public void updateNVRDeviceList(final Handler handler, String deviceId) {
        FormBody formBody = new FormBody
                .Builder()
                .add("deviceId", deviceId)
                .build();
        Request request = new Request.Builder()
                .url(updateUrl)
                .post(formBody)
                .build();
        Call call = mHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) {
                String responseMsg = null;
                try {
                    responseMsg = response.body().string();
                    if(MyUtil.isJson(responseMsg)){
                        Message msg = handler.obtainMessage();
                        msg.what = NVR_UPDATE_WHAT;
                        msg.obj = responseMsg;
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, responseMsg);
            }
        });
    }

}
