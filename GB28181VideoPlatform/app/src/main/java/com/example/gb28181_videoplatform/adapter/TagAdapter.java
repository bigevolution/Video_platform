package com.example.gb28181_videoplatform.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.util.PixelTool;
import com.example.gb28181_videoplatform.widget.flow.OnInitSelectedPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HanHailong on 15/10/19.
 * 复选框数据适配器
 */
public class TagAdapter<T> extends BaseAdapter implements OnInitSelectedPosition {

    private final Context mContext;
    private final List<T> mDataList;
    private String text;

    public TagAdapter(Context context) {
        this.mContext = context;
        mDataList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.flow_item_view, null);

        TextView textView = view.findViewById(R.id.flow_text);
        T t = mDataList.get(position);
        if (t instanceof String) {
            text = (String) t;
        }
        //针对字数少的标签动态设置宽高
        if(text.length() == 2){
            textView.setPadding(PixelTool.dpToPx(mContext,16), PixelTool.dpToPx(mContext,5),
                    PixelTool.dpToPx(mContext,16), PixelTool.dpToPx(mContext,5));
        }else if(text.length() == 3){
            textView.setPadding(PixelTool.dpToPx(mContext,10), PixelTool.dpToPx(mContext,5),
                    PixelTool.dpToPx(mContext,10), PixelTool.dpToPx(mContext,5));
        }else {
            textView.setPadding(PixelTool.dpToPx(mContext,5), PixelTool.dpToPx(mContext,5),
                    PixelTool.dpToPx(mContext,5), PixelTool.dpToPx(mContext,5));
        }
        textView.setText(text);

        return view;
    }

    public void onlyAddAll(List<T> datas) {
        mDataList.addAll(datas);
        notifyDataSetChanged();
    }

    public void clearAndAddAll(List<T> datas) {
        mDataList.clear();
        onlyAddAll(datas);
    }

    @Override
    public boolean isSelectedPosition(int position) {
        if (position % 2 == 0) {
            return true;
        }
        return false;
    }
}
