package com.example.gb28181_videoplatform.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.util.MyUtil;

/**
 * Created by 吴迪 on 2019/8/1.
 * 基类Activity
 */
public abstract class BaseActivity extends Activity {

    private LinearLayout base_title_group;
    private TextView base_title_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtil.immersiveNotificationBar(this, R.color.activity_title);
        super.setContentView(R.layout.activity_base);

        base_title_group = findViewById(R.id.base_title_group);
        base_title_text = findViewById(R.id.base_title_text);

    }

    /**
     * 重点是重写setContentView，让继承者可以继续设置setContentView
     */
    @Override
    public void setContentView(int resId) {
        View view = getLayoutInflater().inflate(resId, null);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.BELOW, R.id.base_title_group);
        if (null != base_title_group)
            base_title_group.addView(view, lp);
    }

    /**
     * 设置中间标题文字
     */
    public void setTitleText(CharSequence c) {
        if (base_title_text != null)
            base_title_text.setText(c);
    }

    /**
     * 关闭当前页面
     */
    public void back(View view) {
        finish();
    }

}