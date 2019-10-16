package com.example.gb28181_videoplatform.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.activity.LiveVideoActivity;
import com.example.gb28181_videoplatform.activity.NVRAisleDeviceActivity;
import com.example.gb28181_videoplatform.adapter.DeviceAdapter;
import com.example.gb28181_videoplatform.adapter.TagAdapter;
import com.example.gb28181_videoplatform.bean.DeviceBean;
import com.example.gb28181_videoplatform.http.OkHttpManager;
import com.example.gb28181_videoplatform.netty.util.PoliceService;
import com.example.gb28181_videoplatform.netty.util.PoliceServiceListener;
import com.example.gb28181_videoplatform.sip.impl.DeviceImpl;
import com.example.gb28181_videoplatform.util.ConstantConfig;
import com.example.gb28181_videoplatform.util.Toasty;
import com.example.gb28181_videoplatform.widget.flow.FlowTagLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.gb28181_videoplatform.util.MyUtil.closeAnimation;
import static com.example.gb28181_videoplatform.util.MyUtil.controlTwoView;
import static com.example.gb28181_videoplatform.util.MyUtil.initFlowData;
import static com.example.gb28181_videoplatform.util.MyUtil.initFlowLayout;
import static com.example.gb28181_videoplatform.util.MyUtil.showAnimation;

/**
 * Created by 吴迪 on 2019/7/18.
 * 设备列表页面
 */
public class DeviceFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "111wd";
    private static final String DEVICE_TYPE = "type";
    private static final String FRAGMENT_NUM = "accessType";

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
    private FlowTagLayout mNVRFlowTagLayout;
    TagAdapter<String> mIPCTagAdapter;
    TagAdapter<String> mSmartTagAdapter;
    TagAdapter<String> mNVRTagAdapter;
    LinearLayout ipc_layout, smart_layout, nvr_layout;
    TextView tv_type_confirm, tv_type_reset;
    View close_type_view;
    boolean isTypeFlow = false;

    //动画
    Animation mShowAction, mCloseAction;

    //接口所需的参数
    String deviceType;
    String accessType;
    String status = "";
    String ptzType = "";

    boolean isViewCreate = false ;
    boolean isOnLoad = true;

    //字符串拼接
    StringBuilder statusString;
    StringBuilder ipcString;
    StringBuilder smartString;
    StringBuilder nvrString;

    //页码数
    int pageNo = 1;

    //每次加载的条数
    int pageSize = 10;

    public static DeviceFragment newInstance(String type, String num) {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_TYPE, type);
        args.putString(FRAGMENT_NUM, num);
        fragment.setArguments(args);
        return fragment;
    }

    //设备类型数据回调
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
                case OkHttpManager.DEVICE_TYPE_NVR:
                    initFlowData(mNVRTagAdapter, data, false);
                    break;
            }
        }
    };

    //设备列表数据回调
    @SuppressLint("HandlerLeak")
    Handler deviceListHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case OkHttpManager.DEVICE_LIST_WHAT:
                    refresh_Layout.finishRefresh();
                    refresh_Layout.finishLoadMore();
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
                    Toasty.error(getActivity(), "网络异常，请稍后再试", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreate = true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isViewCreate && getUserVisibleHint()) {
            onLoadData();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common,container,false);

        initView(view);
        initFlow(view);
        setAnimation();

        return view;
    }

    private void initView(View view) {
        device_state = view.findViewById(R.id.device_state);
        device_state.setOnClickListener(this);
        device_type = view.findViewById(R.id.device_type);
        device_type.setOnClickListener(this);
        device_list = view.findViewById(R.id.device_list);
        refresh_Layout = view.findViewById(R.id.refreshLayout);
        null_view = view.findViewById(R.id.no_data);
        close_state_view = view.findViewById(R.id.close_state_view);
        close_state_view.setOnClickListener(this);
        close_type_view = view.findViewById(R.id.close_type_view);
        close_type_view.setOnClickListener(this);

        //上拉加载监听
        refresh_Layout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                pageNo ++;
                OkHttpManager.getInstance().getDeviceList(deviceListHandler, status, accessType, ptzType, pageNo, pageSize);
            }
        });
        videoList = new ArrayList<>();

    }

    private void initFlow(View view) {
        mStateFlowTagLayout = view.findViewById(R.id.state_flow_view);
        mIPCFlowTagLayout = view.findViewById(R.id.ipc_flow_view);
        mSmartFlowTagLayout = view.findViewById(R.id.smart_flow_view);
        mNVRFlowTagLayout = view.findViewById(R.id.nvr_flow_view);

        TagAdapter<String> mStateTagAdapter = new TagAdapter<>(getContext());
        statusString = initFlowLayout(mStateTagAdapter, mStateFlowTagLayout, FlowTagLayout.FLOW_TAG_CHECKED_SINGLE, true);
        initFlowData(mStateTagAdapter, null, true);

        mIPCTagAdapter = new TagAdapter<>(getContext());
        ipcString = initFlowLayout(mIPCTagAdapter, mIPCFlowTagLayout, FlowTagLayout.FLOW_TAG_CHECKED_MULTI, false);

        mSmartTagAdapter = new TagAdapter<>(getContext());
        smartString = initFlowLayout(mSmartTagAdapter, mSmartFlowTagLayout, FlowTagLayout.FLOW_TAG_CHECKED_MULTI, false);

        mNVRTagAdapter = new TagAdapter<>(getContext());
        nvrString = initFlowLayout(mNVRTagAdapter, mNVRFlowTagLayout, FlowTagLayout.FLOW_TAG_CHECKED_MULTI, false);

        flow_state_layout = view.findViewById(R.id.flow_state_layout);
        tv_state_confirm = view.findViewById(R.id.tv_state_confirm);
        tv_state_confirm.setOnClickListener(this);
        tv_state_reset = view.findViewById(R.id.tv_state_reset);
        tv_state_reset.setOnClickListener(this);

        flow_type_layout = view.findViewById(R.id.flow_type_layout);
        tv_type_confirm = view.findViewById(R.id.tv_type_confirm);
        tv_type_confirm.setOnClickListener(this);
        tv_type_reset = view.findViewById(R.id.tv_type_reset);
        tv_type_reset.setOnClickListener(this);

        ipc_layout = view.findViewById(R.id.ipc_layout);

        smart_layout = view.findViewById(R.id.smart_layout);
        nvr_layout = view.findViewById(R.id.nvr_layout);

        //根据类型请求接口
        accessType = getArguments().getString(FRAGMENT_NUM);
        deviceType = getArguments().getString(DEVICE_TYPE);
        if(deviceType != null){
            switch (deviceType) {
                case ConstantConfig.DEVICE_IPC:
                    OkHttpManager.getInstance().getDeviceType(deviceTypeHandler, 1);
                    controlTwoView(smart_layout, nvr_layout, false);
                    break;
                case ConstantConfig.DEVICE_SMART:
                    OkHttpManager.getInstance().getDeviceType(deviceTypeHandler, 2);
                    controlTwoView(ipc_layout, nvr_layout, false);
                    smart_layout.setVisibility(View.VISIBLE);
                    break;
                case ConstantConfig.DEVICE_NVR:
                    device_type.setVisibility(View.GONE);
                    break;
                default:
                    controlTwoView(smart_layout, nvr_layout, true);
                    OkHttpManager.getInstance().getDeviceType(deviceTypeHandler, 1);
                    OkHttpManager.getInstance().getDeviceType(deviceTypeHandler, 2);
                    OkHttpManager.getInstance().getDeviceType(deviceTypeHandler, 3);
                    break;
            }
        }

    }

    private void onLoadData() {
        if(isOnLoad) {
            isOnLoad = false;
            device_list.setLayoutManager(new GridLayoutManager(getContext(), 2));
            device_Adapter = new DeviceAdapter(getActivity(), R.layout.device_list_item, videoList, deviceType);
            device_Adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    DeviceBean.DataBean.AppDeviceInfoBean infoBean = videoList.get(position);
                    if(!infoBean.getPtzType().equals(getString(R.string.tab_NVR))){
                        if(infoBean.getStatus() == 0){
                            Toasty.error(getActivity(), getString(R.string.device_offline), Toast.LENGTH_SHORT).show();
                        } else if(infoBean.getDeviceId().equals(DeviceImpl.getInstance().getSipProfile().getMySipNum())){
                            Toasty.error(getActivity(), getString(R.string.own_device), Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(getActivity(), LiveVideoActivity.class);
                            intent.putExtra(ConstantConfig.DEVICE_LIVE, infoBean);
                            intent.putExtra(ConstantConfig.DEVICE_IS_NVR, false);
                            startActivity(intent);
                        }
                    }else {
                        if(infoBean.getStatus() == 0){
                            Toasty.error(getActivity(), getString(R.string.nvr_offline), Toast.LENGTH_SHORT).show();
                        }else {
                            Intent intent = new Intent(getActivity(), NVRAisleDeviceActivity.class);
                            intent.putExtra(ConstantConfig.DEVICE_NVR, infoBean.getDeviceId());
                            startActivity(intent);
                        }
                    }
                }
            });
            device_list.setAdapter(device_Adapter);
            //自动刷新
            refresh_Layout.autoRefresh();
            //下拉刷新监听
            refresh_Layout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    pageNo = 1;
                    videoList.clear();
                    OkHttpManager.getInstance().getDeviceList(deviceListHandler, status, accessType, ptzType, pageNo, pageSize);
                }
            });
        }

    }

    PoliceServiceListener listener = new PoliceServiceListener(){
        @Override
        public void onUpdateDevice(String msg) {
            //收到更新设备消息进行处理
            JsonElement element = new JsonParser().parse(msg);
            JsonObject object = element.getAsJsonObject();
            String id = object.get("DeviceID").getAsString();
            String status = object.get("status").getAsString();
            for (int i = 0; i < videoList.size(); i++) {
                if(videoList.get(i).getDeviceId().equals(id) || videoList.get(i).getParentId().equals(id)){
                    if(status.equals("ON")){
                        videoList.get(i).setStatus(1);
                    }else {
                        videoList.get(i).setStatus(0);
                    }
                    device_Adapter.notifyItemChanged(i);
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        PoliceService.instance.addListener(listener);
        PoliceService.instance.onResume();
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //根据fragment可见状态关闭复选框
        if(isVisibleToUser && isViewCreate){
            onLoadData();
            if(flow_state_layout != null && flow_type_layout != null){
                if(flow_state_layout.getVisibility() == View.VISIBLE){
                    flow_state_layout.setVisibility(View.GONE);
                    isStateFlow = false;
                }else if(flow_type_layout.getVisibility() == View.VISIBLE){
                    flow_type_layout.setVisibility(View.GONE);
                    isTypeFlow = false;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.device_state://状态框
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
            case R.id.tv_state_confirm://状态框确定
                closeAnimation(mCloseAction, flow_state_layout);
                isStateFlow = false;
                videoList.clear();
                pageNo = 1;
                status = statusString.toString();
                device_state.setText(status.equals("") ? getString(R.string.device_state_select) : status);
                device_state.setBackgroundResource(status.equals("") ? 0 : R.drawable.flow_text_bg);
                OkHttpManager.getInstance().getDeviceList(deviceListHandler, status, accessType, ptzType, pageNo, pageSize);
                break;
            case R.id.tv_state_reset://状态框重置
                mStateFlowTagLayout.clearAllOption();
                statusString.delete(0, statusString.length());
                status = "";
                device_state.setText(getString(R.string.device_state_select));
                device_state.setBackgroundResource(0);
                break;
            case R.id.device_type://类型框
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
            case R.id.tv_type_confirm://类型框确定
                closeAnimation(mCloseAction, flow_type_layout);
                isTypeFlow = false;
                videoList.clear();
                pageNo = 1;
                //将三个复选框选中的标签进行拼接
                ptzType = ipcString.toString() + smartString.toString() + nvrString.toString();
                device_type.setText(ptzType.equals("") ? getString(R.string.device_type_select) : ptzType.replace("'",""));
                device_type.setBackgroundResource(ptzType.equals("") ? 0 : R.drawable.flow_text_bg);
                OkHttpManager.getInstance().getDeviceList(deviceListHandler, status, accessType, ptzType, pageNo, pageSize);
                break;
            case R.id.tv_type_reset://类型框重置
                mIPCFlowTagLayout.clearAllOption();
                mSmartFlowTagLayout.clearAllOption();
                mNVRFlowTagLayout.clearAllOption();
                ipcString.delete(0, ipcString.length());
                smartString.delete(0, smartString.length());
                nvrString.delete(0, nvrString.length());
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
                OkHttpManager.getInstance().getDeviceList(deviceListHandler, status, accessType, ptzType, pageNo, pageSize);
                break;
            case R.id.close_type_view://关闭类型框
                closeAnimation(mCloseAction, flow_type_layout);
                isTypeFlow = false;
                videoList.clear();
                pageNo = 1;
                //将三个复选框选中的标签进行拼接
                ptzType = ipcString.toString() + smartString.toString() + nvrString.toString();
                device_type.setText(ptzType.equals("") ? getString(R.string.device_type_select) : ptzType.replace("'",""));
                device_type.setBackgroundResource(ptzType.equals("") ? 0 : R.drawable.flow_text_bg);
                OkHttpManager.getInstance().getDeviceList(deviceListHandler, status, accessType, ptzType, pageNo, pageSize);
                break;
            default:
                break;
        }
    }

}
