package com.example.gb28181_videoplatform.netty.util;

/**
 * 统一消息码
 *
 */
public enum CODE {
	success("200","成功"),

	/**
	 *  推送相关类型定义  开始
	 */
	PUSHSEND("3001","已推送"),
	PUSHSUCCESS("3002","已送达"),
	PUSHFAIL("3003","已失败"),
	PUSHOFFLINE("3004","拉取离线"),
	/**
	 *  推送相关类型定义 结束
	 */


	/**
	 *  眼睛相关类型定义  开始
	 */
	PACKETYPE_LoginResult("70001","登录"),
	PACKETYPE_NormalMessage("70002","异常通知"),
	PACKETYPE_START_VIDEO("70003","发起语音or视频通话"),
	PACKETYPE_TAKE_PICTURE_UPLOAD("70004","拍照上传"),
	PACKETYPE__ACTION_TASK("70005","行动任务"),
	PACKETYPE_Notification("70007","通知"),
	PACKETYPE_UPLOAD_PICTURE("70008","图片上传"),
	PACKETYPE__REGISTERED("70009","客户端注册"),
	PACKETYPE__REPORT_MAC("70010","上报MAC信息"),
	PACKETYPE__REPORT_LOCATION("70011","上报位置信息"),
	PACKETYPE__REPORT_HEART_BEAT("70013","心跳包"),
	PACKETYPE__PATROL_SIGNAL("70014","开始or结束巡逻接口"),
	PACKETYPE__AUDIO_MESSAGE("70015","语音文本消息"),
	PACKETYPE__DOCUMENT_COLLECT("70016","证件采集"),
	PACKETYPE__VIDEO_PUSH_FLOW("70017","视频推流"),
	PACKETYPE__TAKE_PICTURE("70018","证件采集"),
	PACKETYPE__DEVICE_OFFLINE("70019","设备上下线"),
	PACKETYPE__DEVICE_LOCATION("70020","设备位置更新消息"),
	PACKETYPE__RECORD_SCREEN("70023","推送屏幕录屏"),
	PACKETYPE__PUSH_FLY("70026","飞投"),
	PACKETYPE__UPDATE_DEVICE("20000","设备上下线"),
	PACKETYPE__PULL_ADDRESS("2011","拉取推流地址"),
	PACKETYPE__COMMAND_SCHEDULING("80202","指挥调度请求"),
	PACKETYPE__COMMAND_SCHEDULING_RSP("80203","指挥调度接收"),
	/**
	 *  眼睛相关类型定义  结束
	 */


	parameters_incorrect("400","参数不正确"),
	parameters_invalid("401","特定参数不符合条件没有这个用户"),
	service_notfound("402","没有这个服务"),
	node_unavailable("403","没有可用的服务节点"),

	error("500","执行错误"),
	authentication_fail("501","认证失败"),
	roles_fail("502","授权失败"),
	session_expiration("503","Session 过期"),
	session_lose("504","Session 丢失"),

	timeout("510","调用超时"),
	generate_return_error("511","处理返回值错误"),
	limit("512","接口调用次数超过限制"),
	limit_by_group("513","用户调用次数超过限制"),

	online("1001","客户端上线请求"),
	send_message("1002","客户端发送'发送消息'请求"),
	receive_message("1003","服务端发送'接收消息'请求"),
	downline("1004","客户端下线请求"),
	notify_online_status("1005","通知页面警员上下线"),
	client_media_request("1006","通知页面警员媒体请求"),
	busOnline("1008","公交车上线请求"),
	busdownline("1009","公交车下线请求"),
	update_location("1007","更新位置请求");




	public String note;
	public String code;

	private CODE(String code, String note) {
		this.note = note;
		this.code = code;
	}

	// 普通方法
	public static String getNote(String code) {
		for (CODE c : CODE.values()) {
			if (code.equals(c.getCode())) {
				return c.note;
			}
		}
		return null;
	}
	// 普通方法
	public static String getCode(String note) {
		for (CODE c : CODE.values()) {
			if (note.equals(c.getNote())) {
				return c.code;
			}
		}
		return null;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
