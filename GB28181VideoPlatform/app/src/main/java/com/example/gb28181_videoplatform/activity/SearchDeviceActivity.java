package com.example.gb28181_videoplatform.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.adapter.DeviceAdapter;
import com.example.gb28181_videoplatform.adapter.TagAdapter;
import com.example.gb28181_videoplatform.bean.DeviceBean;
import com.example.gb28181_videoplatform.http.OkHttpManager;
import com.example.gb28181_videoplatform.sip.impl.DeviceImpl;
import com.example.gb28181_videoplatform.util.ConstantConfig;
import com.example.gb28181_videoplatform.util.Toasty;
import com.example.gb28181_videoplatform.widget.flow.FlowTagLayout;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.gb28181_videoplatform.util.MyUtil.closeAnimation;
import static com.example.gb28181_videoplatform.util.MyUtil.initFlowData;
import static com.example.gb28181_videoplatform.util.MyUtil.initFlowLayout;
import static com.example.gb28181_videoplatform.util.MyUtil.showAnimation;

/**
 * Created by 吴迪 on 2019/7/10.
 * 搜索结果Activity （主要根据传入的设备名称进行操作）
 */
public class SearchDeviceActivity extends BaseActivity implements View.OnClickListener{

    //设备列表相关
    ImageView null_view;
    RecyclerView device_list;
    SmartRefreshLayout refresh_Layout;
    BaseQuickAdapter device_Adapter;
    List<DeviceBean.DataBean.AppDeviceInfoBean> videoList;

    //状态框相关
    TextView device_state;
    RelativeLayout flow_state_layout;
    private FlowTagLayout mStateFlowTagLayout;
    TextView tv_state_confirm, tv_state_reset;
    boolean isStateFlow = false;
    View close_state_view;

    //类型框相关
    TextView device_type;
    RelativeLayout flow_type_layout;
    private FlowTagLayout mIPCFlowTagLayout;
    private FlowTagLayout mSmartFlowTagLayout;
    TagAdapter<String> mIPCTagAdapter;
    TagAdapter<String> mSmartTagAdapter;
    LinearLayout smart_layout;
    TextView tv_type_confirm, tv_type_reset;
    View close_type_view;
    boolean isTypeFlow = false;

    //动画
    Animation mShowAction, mCloseAction;

    //搜索的设备名称
    String deviceName;

    String deviceType;
    String status = "";
    String ptzType = "";

    //字符串拼接
    StringBuilder statusString;
    StringBuilder ipcString;
    StringBuilder smartString;

    //页码数
    int pageNo = 1;

    //每次加载的条数
    int pageSize = 10;

    @SuppressLint("HandlerLeak")
    Handler deviceTypeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            List<String> data = (List<String>) msg.obj;
            switch (msg.what) {
                case OkHttpManager.DEVICE_TYPE_IPC:
                    initFlowData(mIPCTagAdapter, data, false);
                    break;
                case OkHttpManager.DEVICE_TYPE_SMART:
                    initFlowData(mSmartTagAdapter, data, false);
                    break;
            }
        }
    };

    @SuppressLint("HandlerLeak")
    Handler deviceListHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case OkHttpManager.DEVICE_LIST_WHAT:
                    videoList.addAll((List<DeviceBean.DataBean.AppDeviceInfoBean>)msg.obj);
                    if(videoList.size() == 0){
                        null_view.setVisibility(View.VISIBLE);
                    }else {
                        null_view.setVisibility(View.GONE);
                    }
                    device_Adapter.notifyDataSetChanged();
                    break;
                case OkHttpManager.DEVICE_REQUEST_ERROR:
                    refresh_Layout.finishRefresh(false);
                    refresh_Layout.finishLoadMore(false);
                    videoList.clear();
                    device_Adapter.notifyDataSetChanged();
                    Toasty.error(SearchDeviceActivity.this, "网络异常，请稍后再试", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);

        initView();
        initFlow();
        initAdapter();
        setAnimation();

    }

    private void initView() {
        setTitleText(getString(R.string.search_result));
        refresh_Layout = findViewById(R.id.refreshLayout);
        device_list = findViewById(R.id.device_list);
        device_list.setLayoutManager(new GridLayoutManager(this, 2));
        videoList = new ArrayList<>();
        close_state_view = findViewById(R.id.close_state_view);
        close_state_view.setOnClickListener(this);
        close_type_view = findViewById(R.id.close_type_view);
        close_type_view.setOnClickListener(this);

        //下拉刷新监听
        refresh_Layout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh(1000/*,false*/);//传入false表示刷新失败
                pageNo = 1;
                videoList.clear();
                OkHttpManager.getInstance().getSearchDevice(deviceListHandler, deviceName, status, "", ptzType, pageNo, pageSize);
            }
        });

        //上拉加载监听
        refresh_Layout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                refreshlayout.finishLoadMore(1000/*,false*/);//传入false表示加载失败
                pageNo ++;
                OkHttpManager.getInstance().getSearchDevice(deviceListHandler, deviceName, status, "", ptzType, pageNo, pageSize);
            }
        });

        deviceName = getIntent().getStringExtra(ConstantConfig.DEVICE_SEARCH);
        OkHttpManager.getInstance().getDeviceType(deviceTypeHandler, 1);
        OkHttpManager.getInstance().getDeviceType(deviceTypeHandler, 2);
        OkHttpManager.getInstance().getSearchDevice(deviceListHandler, deviceName, status, "", ptzType, pageNo, pageSize);

    }

    private void initFlow() {
        device_state = findViewById(R.id.device_state);
        device_state.setOnClickListener(this);
        device_type = findViewById(R.id.device_type);
        device_type.setOnClickListener(this);
        null_view = findViewById(R.id.no_data);
        smart_layout = findViewById(R.id.smart_layout);
        smart_layout.setVisibility(View.VISIBLE);

        mStateFlowTagLayout = findViewById(R.id.state_flow_view);
        mIPCFlowTagLayout = findViewById(R.id.ipc_flow_view);
        mSmartFlowTagLayout = findViewById(R.id.smart_flow_view);

        TagAdapter<String> mStateTagAdapter = new TagAdapter<>(this);
        statusString = initFlowLayout(mStateTagAdapter, mStateFlowTagLayout, FlowTagLayout.FLOW_TAG_CHECKED_SINGLE, true);
        initFlowData(mStateTagAdapter, null, true);

        mIPCTagAdapter = new TagAdapter<>(getApplication());
        ipcString = initFlowLayout(mIPCTagAdapter, mIPCFlowTagLayout, FlowTagLayout.FLOW_TAG_CHECKED_MULTI, false);

        mSmartTagAdapter = new TagAdapter<>(getApplication());
        smartString = initFlowLayout(mSmartTagAdapter, mSmartFlowTagLayout, FlowTagLayout.FLOW_TAG_CHECKED_MULTI, false);

        flow_state_layout = findViewById(R.id.flow_state_layout);
        tv_state_confirm = findViewById(R.id.tv_state_confirm);
        tv_state_confirm.setOnClickListener(this);
        tv_state_reset = findViewById(R.id.tv_state_reset);
        tv_state_reset.setOnClickListener(this);

        flow_type_layout = findViewById(R.id.flow_type_layout);
        tv_type_confirm = findViewById(R.id.tv_type_confirm);
        tv_type_confirm.setOnClickListener(this);
        tv_type_reset = findViewById(R.id.tv_type_reset);
        tv_type_reset.setOnClickListener(this);
    }

    private void initAdapter() {
        device_Adapter = new DeviceAdapter(SearchDeviceActivity.this, R.layout.device_list_item, videoList, deviceType);
        device_Adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                DeviceBean.DataBean.AppDeviceInfoBean infoBean = videoList.get(position);
                if(!infoBean.getPtzType().equals(getString(R.string.tab_NVR))){
                    if(infoBean.getStatus() == 0){
                        Toasty.error(SearchDeviceActivity.this, getString(R.string.device_offline), Toast.LENGTH_SHORT).show();
                    } else if(infoBean.getDeviceId().equals(DeviceImpl.getInstance().getSipProfile().getMySipNum())){
                        Toasty.error(SearchDeviceActivity.this, getString(R.string.own_device), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(SearchDeviceActivity.this, LiveVideoActivity.class);
                        intent.putExtra(ConstantConfig.DEVICE_LIVE, infoBean);
                        startActivity(intent);
                    }
                }else {
                    if(infoBean.getStatus() == 0){
                        Toasty.error(SearchDeviceActivity.this, getString(R.string.nvr_offline), Toast.LENGTH_SHORT).show();
                    }else {
                        Intent intent = new Intent(SearchDeviceActivity.this, NVRAisleDeviceActivity.class);
                        intent.putExtra(ConstantConfig.DEVICE_NVR, infoBean.getDeviceId());
                        startActivity(intent);
                    }
                }
            }
        });
        device_list.setAdapter(device_Adapter);
    }

    public void setAnimation() {
        //设置显示时的动画
        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(300);
        //设置隐藏时的动画，监听动画结束后隐藏选择框
        mCloseAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
        mCloseAction.setDuration(300);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.device_state:
                if(flow_type_layout.getVisibility() == View.VISIBLE){
                    closeAnimation(mCloseAction, flow_type_layout);
                    showAnimation(mShowAction, flow_state_layout);
                    isTypeFlow = false;
                }else {
                    if(isStateFlow){
                        closeAnimation(mCloseAction, flow_state_layout);
                    }else {
                        showAnimation(mShowAction, flow_state_layout);
                    }
                }
                isStateFlow = !isStateFlow;
                break;
            case R.id.tv_state_confirm:
                closeAnimation(mCloseAction, flow_state_layout);
                isStateFlow = false;
                videoList.clear();
                pageNo = 1;
                status = statusString.toString();
                device_state.setText(status.equals("") ? getString(R.string.device_state_select) : status);
                device_state.setBackgroundResource(status.equals("") ? 0 : R.drawable.flow_text_bg);
                OkHttpManager.getInstance().getSearchDevice(deviceListHandler, deviceName, status, "", ptzType, pageNo, pageSize);
                break;
            case R.id.tv_state_reset:
                mStateFlowTagLayout.clearAllOption();
                statusString.delete(0, statusString.length());
                status = "";
                device_state.setText(getString(R.string.device_state_select));
                device_state.setBackgroundResource(0);
                break;
            case R.id.device_type:
                if(flow_state_layout.getVisibility() == View.VISIBLE){
                    closeAnimation(mCloseAction, flow_state_layout);
                    showAnimation(mShowAction, flow_type_layout);
                    isStateFlow = false;
                }else {
                    if(isTypeFlow){
                        closeAnimation(mCloseAction, flow_type_layout);
                    }else {
                        showAnimation(mShowAction, flow_type_layout);
                    }
                }
                isTypeFlow = ! isTypeFlow;
                break;
            case R.id.tv_type_confirm:
                closeAnimation(mCloseAction, flow_type_layout);
                isTypeFlow = false;
                videoList.clear();
                pageNo = 1;
                ptzType = ipcString.toString() + smartString.toString();
                device_type.setText(ptzType.equals("") ? getString(R.string.device_type_select) : ptzType.replace("'",""));
                device_type.setBackgroundResource(ptzType.equals("") ? 0 : R.drawable.flow_text_bg);
                OkHttpManager.getInstance().getSearchDevice(deviceListHandler, deviceName, status, "", ptzType, pageNo, pageSize);
                break;
            case R.id.tv_type_reset:
                mIPCFlowTagLayout.clearAllOption();
                mSmartFlowTagLayout.clearAllOption();
                ipcString.delete(0, ipcString.length());
                smartString.delete(0, smartString.length());
                ptzType = "";
                device_type.setText(getString(R.string.device_type_select));
                device_type.setBackgroundResource(0);
                break;
            case R.id.close_state_view://关闭状态框
                closeAnimation(mCloseAction, flow_state_layout);
                isStateFlow = false;
                videoList.clear();
                pageNo = 1;
                status = statusString.toString();
                device_state.setText(status.equals("") ? getString(R.string.device_state_select) : status);
                device_state.setBackgroundResource(status.equals("") ? 0 : R.drawable.flow_text_bg);
                OkHttpManager.getInstance().getDeviceList(deviceListHandler, status, "", ptzType, pageNo, pageSize);
                break;
            case R.id.close_type_view://关闭类型框
                closeAnimation(mCloseAction, flow_type_layout);
                isTypeFlow = false;
                videoList.clear();
                pageNo = 1;
                //将三个复选框选中的标签进行拼接
                ptzType = ipcString.toString() + smartString.toString();
                device_type.setText(ptzType.equals("") ? getString(R.string.device_type_select) : ptzType.replace("'",""));
                device_type.setBackgroundResource(ptzType.equals("") ? 0 : R.drawable.flow_text_bg);
                OkHttpManager.getInstance().getDeviceList(deviceListHandler, status,"", ptzType, pageNo, pageSize);
                break;
            default:
                break;
        }
    }

}

