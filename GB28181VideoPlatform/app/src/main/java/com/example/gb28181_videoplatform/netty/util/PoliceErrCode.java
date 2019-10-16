package com.example.gb28181_videoplatform.netty.util;

import java.util.Locale;

public class PoliceErrCode {

	public static final int CREATE_GROUP_SUCCESS = 0;
	public static final int SUCCESS = 200;
	
	public static final int ERROR = 404;
	public static final int DEVICE_DISABLED = 500;
	public static final int SERVER_CONNECT_FAIL = 501; 	
	public static final int SERVER_RSP_FORMAT_ERROR = 502;
	
	/**读写文件失败*/
	public static final int FILE_IO_FAIL = 5009; 
	/**用户取消文档上传*/
	public static final int USER_CANCEL_UPLOAD = 5010;
	/**超时*/
	public static final int TIMEOUT = 5011; 
	/**上传文件失败*/
	public static final int UPLOAD_FAIL = 5012; 
	/**下载文件失败*/
	public static final int DOWNLOAD_FAIL = 5013;
	/**用户取消文档下载*/
	public static final int USER_CANCEL_DOWNLOAD = 5014;
	
	public static final int ACCOUNT_EXISTS = 6001;//警员号已经存在
	public static final int DATABASE_FAIL = 6002;
	public static final int POLICE_NOT_EXISTS = 6003;// 警员号不存在
	public static final int RE_LOGIN_IN = 6004;// 重新登陆
	public static final String SESSION_ID_FAIL = "b170147fc51d4b4cb91d07eb34cc2841"; //获取SESSION_ID失败

	public static final String request_locale = "request_locale";
	public static final String locale = Locale.getDefault().getLanguage();//传入当前默认语言

}
