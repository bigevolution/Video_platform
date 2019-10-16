package com.example.gb28181_videoplatform.sip;

import android.gov.nist.javax.sip.clientauthutils.AccountManager;
import android.gov.nist.javax.sip.clientauthutils.UserCredentials;
import android.javax.sip.ClientTransaction;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;

public class AccountManagerImpl implements AccountManager {

    private static final String TAG = "AccountManagerImpl";
    private String userName = SPUtils.getInstance().getString("accountName");
    private String password = SPUtils.getInstance().getString("accountPassword");

    public UserCredentials getCredentials(ClientTransaction challengedTransaction, String realm) {
        Log.e("gaozy", "userName== " + userName + ":::password==" + password);
        return new UserCredentialsImpl(userName, "nist.gov", password);// 12345678,123456

        //return new UserCredentialsImpl(DeviceImpl.getInstance().getSipProfile().getSipUserName(), "nist.gov", DeviceImpl.getInstance().getSipProfile().getSipPassword());// 12345678,123456
    }
}