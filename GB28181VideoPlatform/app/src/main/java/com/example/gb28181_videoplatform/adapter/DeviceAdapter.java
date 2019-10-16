package com.example.gb28181_videoplatform.adapter;

import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.activity.NVRAisleDeviceActivity;
import com.example.gb28181_videoplatform.activity.PlaybackVideoActivity;
import com.example.gb28181_videoplatform.bean.DeviceBean;
import com.example.gb28181_videoplatform.util.ConstantConfig;
import com.example.gb28181_videoplatform.util.PixelTool;
import com.example.gb28181_videoplatform.util.Toasty;

import java.util.List;

import static com.example.gb28181_videoplatform.util.MyUtil.setViewParams;

/**
 * Created by 吴迪 on 2019/7/16.
 * 设备适配器
 */
public class DeviceAdapter extends BaseQuickAdapter<DeviceBean.DataBean.AppDeviceInfoBean, BaseViewHolder> {

    private Activity activity;
    private String deviceType;

    public DeviceAdapter(Activity mActivity, int layoutResId, List<DeviceBean.DataBean.AppDeviceInfoBean> data, String type) {
        super(layoutResId, data);
        activity = mActivity;
        deviceType = type;
    }

    @Override
    protected void convert(BaseViewHolder helper, final DeviceBean.DataBean.AppDeviceInfoBean infoBean) {
        TextView name = helper.getView(R.id.device_name);
        TextView type = helper.getView(R.id.device_type);
        final Button aisle = helper.getView(R.id.aisle_btn);
        Button playback = helper.getView(R.id.base_playback_btn);
        ImageView img = helper.getView(R.id.video_play);
        name.setText(activity.getString(R.string.device_name) + infoBean.getName());
        //根据设备类型判断按钮显示
        if(infoBean.getPtzType().equals(activity.getString(R.string.tab_NVR))){
            type.setText(activity.getString(R.string.aisle) + infoBean.getNum());
            setViewParams(img, PixelTool.dpToPx(activity, 45), PixelTool.dpToPx(activity, 45));
            img.setImageResource(R.mipmap.nvr);
            playback.setVisibility(View.GONE);
            aisle.setVisibility(View.VISIBLE);
            if(infoBean.getStatus() == 0){
                aisle.setBackgroundResource(R.drawable.playback_btn_offline);
                aisle.setTextColor(activity.getResources().getColor(R.color.device_text));
            }else {
                aisle.setBackgroundResource(R.drawable.playback_btn_online);
                aisle.setTextColor(activity.getResources().getColor(R.color.activity_title));
            }
            aisle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(infoBean.getStatus() == 0){
                        Toasty.error(activity, activity.getString(R.string.nvr_offline), Toast.LENGTH_SHORT).show();
                    }else {
                        Intent intent = new Intent(activity, NVRAisleDeviceActivity.class);
                        intent.putExtra(ConstantConfig.DEVICE_NVR, infoBean.getDeviceId());
                        activity.startActivity(intent);
                    }
                }
            });
        }else{
            type.setText(activity.getString(R.string.device_type) + infoBean.getPtzType());
            setViewParams(img, PixelTool.dpToPx(activity, 50), PixelTool.dpToPx(activity, 50));
            img.setImageResource(R.mipmap.camera);
            aisle.setVisibility(View.GONE);
            playback.setVisibility(View.GONE);
            playback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, PlaybackVideoActivity.class);
                    intent.putExtra(ConstantConfig.DEVICE_PLAYBACK, infoBean);
                    activity.startActivity(intent);
                }
            });
        }
        if(deviceType == "NVR_Device"){
            playback.setVisibility(View.VISIBLE);
            playback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, PlaybackVideoActivity.class);
                    intent.putExtra(ConstantConfig.DEVICE_PLAYBACK, infoBean);
                    activity.startActivity(intent);
                }
            });
        }

        //在线离线不同颜色显示
        TextView state = helper.getView(R.id.device_state);
        state.setText(infoBean.getStatus() == 1 ? Html.fromHtml(activity.getString(R.string.device_state) +
                "<font color='#1B9F17'>"+ activity.getString(R.string.device_state_online) + "</font>") : Html.fromHtml(
                        activity.getString(R.string.device_state) + "<font color='#9E9E9E'>"+
                activity.getString(R.string.device_state_offline) + "</font>"));

    }

}
