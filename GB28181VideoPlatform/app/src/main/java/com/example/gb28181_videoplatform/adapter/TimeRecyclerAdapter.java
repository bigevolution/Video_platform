package com.example.gb28181_videoplatform.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.bean.TimeBean;

import java.util.List;

/**
 * Created by Aaron on 2019/9/3.
 */

public class TimeRecyclerAdapter extends RecyclerView.Adapter<TimeRecyclerAdapter.ViewHolder> {
    private List<TimeBean> tList;
    Handler handler = new Handler();
    int selectedPosition = -1;

    public List<TimeBean> gettList() {
        return tList;
    }

    public void settList(List<TimeBean> tList) {
        this.tList = tList;
    }

    public TimeRecyclerAdapter(List<TimeBean> tList, Handler handler){
        this.tList = tList;
        this.handler = handler;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View timeView;
        TextView starthour;
        TextView endhour;
        TextView gang;
        public ViewHolder(View view){
            super(view);
            timeView = view;
            endhour = view.findViewById(R.id.endhour);
            starthour = view.findViewById(R.id.starthour);
            gang = view.findViewById(R.id.gang);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.time_item_view,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        final Context mContext = parent.getContext();
        holder.timeView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                int position = holder.getAdapterPosition();
                selectedPosition = position;
                Message message = new Message();
                message.obj = tList.get(position);
                handler.sendMessage(message);
                notifyDataSetChanged();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        TimeBean time = tList.get(position);
        holder.endhour.setText(time.getEndTime().substring(5));
        holder.starthour.setText(time.getStartTime().substring(5));
        if(selectedPosition == position){
            holder.endhour.setTextColor(Color.parseColor("#F2F2F2"));
            holder.starthour.setTextColor(Color.parseColor("#F2F2F2"));
            holder.gang.setTextColor(Color.parseColor("#F2F2F2"));
            holder.timeView.setBackgroundResource(R.drawable.time_click);
        }else{
            holder.starthour.setTextColor(Color.parseColor("#A3A3A3"));
            holder.endhour.setTextColor(Color.parseColor("#A3A3A3"));
            holder.gang.setTextColor(Color.parseColor("#A3A3A3"));
            holder.timeView.setBackgroundResource(R.drawable.frame_layout);
        }
    }



    @Override
    public int getItemCount(){
        return tList.size();
    }
}
