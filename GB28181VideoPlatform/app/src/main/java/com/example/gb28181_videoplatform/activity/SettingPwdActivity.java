package com.example.gb28181_videoplatform.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.blankj.utilcode.util.SPUtils;
import com.example.gb28181_videoplatform.R;
import com.example.gb28181_videoplatform.util.ConstantConfig;

import static com.example.gb28181_videoplatform.util.MyUtil.setEditText;

/**
 * Created by 吴迪 on 2019/7/10.
 * 设置Activity
 */
public class SettingPwdActivity extends BaseActivity implements View.OnClickListener {

    Button save_btn, confirm_btn;
    LinearLayout password_layout;
    ScrollView setting_layout;
    EditText sip_ip_text, sip_port_text, sip_id_text, sip_pwd_text,
            local_id_text, local_port_text, heart_time_text, payload_type_text;
    EditText web_ip_text, web_port_text, socket_port_text;

    EditText old_pwd_text, new_pwd_text, new_pwd_again_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_pwd);

        initSettingView();
        initPasswordView();

        int page = getIntent().getIntExtra("page",0);
        if(page == ConstantConfig.USER_SETTING){
            setTitleText(getString(R.string.title_setting));
            password_layout.setVisibility(View.GONE);
            setting_layout.setVisibility(View.VISIBLE);
        }else if(page == ConstantConfig.USER_PASSWORD) {
            setTitleText(getString(R.string.title_pwd));
            setting_layout.setVisibility(View.GONE);
            password_layout.setVisibility(View.VISIBLE);
        }

    }

    private void initSettingView(){
        save_btn = findViewById(R.id.save_btn);
        save_btn.setOnClickListener(this);
        setting_layout = findViewById(R.id.setting_layout);

        sip_ip_text = findViewById(R.id.sip_ip_text);
        setEditText(sip_ip_text, getString(R.string.sip_ip_text), 15, SPUtils.getInstance().getString("remoteIp"));

        sip_port_text = findViewById(R.id.sip_port_text);
        setEditText(sip_port_text, getString(R.string.sip_port_text), 15, "" + SPUtils.getInstance().getInt("remotePort"));

        sip_id_text = findViewById(R.id.sip_id_text);
        setEditText(sip_id_text, getString(R.string.sip_id_text), 15, SPUtils.getInstance().getString("sipName"));

        sip_pwd_text = findViewById(R.id.sip_pwd_text);
        setEditText(sip_pwd_text, getString(R.string.sip_pwd_text), 15, SPUtils.getInstance().getString("accountPassword"));

        local_id_text = findViewById(R.id.local_id_text);
        setEditText(local_id_text, getString(R.string.local_id_text), 15, SPUtils.getInstance().getString("mySipName"));

        local_port_text = findViewById(R.id.local_port_text);
        setEditText(local_port_text, getString(R.string.local_port_text), 15, "" + SPUtils.getInstance().getInt("localPort"));

        heart_time_text = findViewById(R.id.heart_time_text);
        setEditText(heart_time_text, getString(R.string.heart_time_text), 15, "" + SPUtils.getInstance().getInt("heartTime"));

        payload_type_text = findViewById(R.id.payload_type_text);
        setEditText(payload_type_text, getString(R.string.payload_type_text), 15, "" + SPUtils.getInstance().getInt("payloadType"));

        web_ip_text = findViewById(R.id.web_ip_text);
        setEditText(web_ip_text, getString(R.string.web_ip_text), 15, SPUtils.getInstance().getString("webIp"));

        web_port_text = findViewById(R.id.web_port_text);
        setEditText(web_port_text, getString(R.string.web_port_text), 15, SPUtils.getInstance().getString("webPort"));

        socket_port_text = findViewById(R.id.socket_port_text);
        setEditText(socket_port_text, getString(R.string.socket_port_text), 15, SPUtils.getInstance().getString("socketPort"));

    }

    private void initPasswordView() {
        confirm_btn = findViewById(R.id.confirm_btn);
        confirm_btn.setOnClickListener(this);
        password_layout = findViewById(R.id.password_layout);

        old_pwd_text = findViewById(R.id.old_pwd_text);
        setEditText(old_pwd_text,getString(R.string.old_pwd_text),15, null);
        new_pwd_text = findViewById(R.id.new_pwd_text);
        setEditText(new_pwd_text,getString(R.string.new_pwd_text),15, null);
        new_pwd_again_text = findViewById(R.id.new_pwd_again_text);
        setEditText(new_pwd_again_text,getString(R.string.new_pwd_again_text),15, null);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save_btn:
                SPUtils.getInstance().put("remoteIp", sip_ip_text.getText().toString());
                SPUtils.getInstance().put("remotePort", Integer.parseInt(sip_port_text.getText().toString()));
                SPUtils.getInstance().put("sipName", sip_id_text.getText().toString());
                SPUtils.getInstance().put("mySipName", local_id_text.getText().toString());
                SPUtils.getInstance().put("accountName", local_id_text.getText().toString());
                SPUtils.getInstance().put("localPort", Integer.parseInt(local_port_text.getText().toString()));
                SPUtils.getInstance().put("accountPassword", sip_pwd_text.getText().toString());
                SPUtils.getInstance().put("heartTime", Integer.parseInt(heart_time_text.getText().toString()));
                SPUtils.getInstance().put("payloadType", Integer.parseInt(payload_type_text.getText().toString()));
                SPUtils.getInstance().put("webIp", web_ip_text.getText().toString());
                SPUtils.getInstance().put("webPort", web_port_text.getText().toString());
                SPUtils.getInstance().put("socketPort", socket_port_text.getText().toString());
                finish();
                break;
            case R.id.confirm_btn:
                finish();
                break;
            default:
                break;
        }
    }

}
