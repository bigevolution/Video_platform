package com.example.gb28181_videoplatform.sip;



public interface ISipManager {

	public void SendMessage(String targetName, String deviceId, String to, String message) throws NotInitializedException;
	public void SendDTMF(String digit) throws NotInitializedException;
	public void Register(int state);
	public void Call(String to, int localRtpPort) throws NotInitializedException;
	public void Hangup() throws NotInitializedException;
	public void SendHeart(String targetName, String to, String message) throws NotInitializedException;
	public void SendBye() throws NotInitializedException;
	public void invite(String to, int port, String nvrSipId, String ipcSipId, boolean isNvr) throws NotInitializedException;
}
