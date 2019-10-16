package com.example.gb28181_videoplatform.sip.impl;

import android.javax.sip.InvalidArgumentException;
import android.javax.sip.SipProvider;
import android.javax.sip.address.Address;
import android.javax.sip.address.AddressFactory;
import android.javax.sip.address.SipURI;
import android.javax.sip.address.URI;
import android.javax.sip.header.CSeqHeader;
import android.javax.sip.header.CallIdHeader;
import android.javax.sip.header.ContactHeader;
import android.javax.sip.header.ContentTypeHeader;
import android.javax.sip.header.ExpiresHeader;
import android.javax.sip.header.FromHeader;
import android.javax.sip.header.HeaderFactory;
import android.javax.sip.header.MaxForwardsHeader;
import android.javax.sip.header.SubjectHeader;
import android.javax.sip.header.ToHeader;
import android.javax.sip.header.ViaHeader;
import android.javax.sip.message.MessageFactory;
import android.javax.sip.message.Request;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;

public class SipRequestMsgBuilder {
    /**
     * 注册以及注销
     *
     * @param sipManager
     * @param state      0：注册，1：注销
     * @return
     * @throws ParseException
     * @throws InvalidArgumentException
     */
    public Request buildRegister(MySipManager sipManager, int state) throws ParseException, InvalidArgumentException {
        AddressFactory addressFactory = sipManager.addressFactory;
        SipProvider sipProvider = sipManager.sipProvider;
        MessageFactory messageFactory = sipManager.messageFactory;
        HeaderFactory headerFactory = sipManager.headerFactory;

        // Create addresses and via header for the request
        Address fromAddress = addressFactory.createAddress("sip:"
                + DeviceImpl.getInstance().getSipProfile().getMySipNum() + "@"
                + DeviceImpl.getInstance().getSipProfile().getLocalIp());
        //fromAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getMySipNum());
        Address toAddress = addressFactory.createAddress("sip:"
                + DeviceImpl.getInstance().getSipProfile().getMySipNum() + "@"
                + DeviceImpl.getInstance().getSipProfile().getLocalIp());
        //toAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getMySipNum());
        Address contactAddress = sipManager.createContactAddress();
        ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();
        URI requestURI = addressFactory.createAddress(
                "sip:" + DeviceImpl.getInstance().getSipProfile().getRemoteSipNum() + "@" + DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint()).getURI();

        // Build the request
        final Request request = messageFactory.createRequest(requestURI,
                Request.REGISTER, sipProvider.getNewCallId(),
                headerFactory.createCSeqHeader(1l, Request.REGISTER),
                headerFactory.createFromHeader(fromAddress, "fh_Gb281818"),
                headerFactory.createToHeader(toAddress, null), viaHeaders,
                headerFactory.createMaxForwardsHeader(70));

        // Add the contact header
        //request.addHeader(headerFactory.createContactHeader(contactAddress));
        if (!sipManager.isFirst) {
            request.addHeader(headerFactory.createContactHeader(contactAddress));
        }
        ExpiresHeader eh = headerFactory.createExpiresHeader(state== 0 ? 3600 : 0);
        request.addHeader(eh);
        // Print the request
        System.out.println(request.toString());

        return request;
    }


    public Request buildMessage(MySipManager sipManager, String targetName, String deviceId, String to, String message) throws ParseException, InvalidArgumentException {
              /*from header*/
        SipURI from = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(), DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
        Address fromNameAddress = sipManager.addressFactory.createAddress(from);
        fromNameAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
        FromHeader fromHeader = sipManager.headerFactory.createFromHeader(fromNameAddress,
                "fh_Gb281818");

        /*to header 对象的地址*/
        //URI toAddress = sipManager.addressFactory.createURI("sip:" + to);
        URI toAddress = sipManager.addressFactory.createSipURI(deviceId, to);
        Address toNameAddress = sipManager.addressFactory.createAddress(toAddress);
        // toNameAddress.setDisplayName(targetName);
        ToHeader toHeader = sipManager.headerFactory.createToHeader(toNameAddress, null);

//        SipURI requestURI = sipManager.addressFactory.createSipURI("server",
//                DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());

        // URI requestURI = sipManager.addressFactory.createURI(to);
        URI requestURI = sipManager.addressFactory.createURI("sip:" + DeviceImpl.getInstance().getSipProfile().getRemoteSipNum()
                + "@" + to);
        // requestURI.setTransportParam("udp");

        ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

        CallIdHeader callIdHeader = sipManager.sipProvider.getNewCallId();

        CSeqHeader cSeqHeader = sipManager.headerFactory.createCSeqHeader(50l,
                Request.MESSAGE);

        MaxForwardsHeader maxForwards = sipManager.headerFactory
                .createMaxForwardsHeader(70);

        Request request = sipManager.messageFactory.createRequest(requestURI,
                Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);
        ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                .createContentTypeHeader("Application", "MANSCDP+xml");
        request.setContent(message, contentTypeHeader);
        System.out.println(request.toString());
        return request;
    }

    public Request buildTimeMsg(MySipManager sipManager, String deviceId, String nvrId, String to, String message) throws ParseException, InvalidArgumentException {
        /*from header*/
        Log.e("BuildRequest", "nvrId:" + nvrId + "-----deviceID:"+ deviceId );
        SipURI from = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(), DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
        Address fromNameAddress = sipManager.addressFactory.createAddress(from);
//        fromNameAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
        FromHeader fromHeader = sipManager.headerFactory.createFromHeader(fromNameAddress,
                "fh_Gb281818");

        /*to header 对象的地址*/
        //URI toAddress = sipManager.addressFactory.createURI("sip:" + to);
//        URI toAddress = sipManager.addressFactory.createSipURI("34020000001180000001", to);
        URI toAddress = sipManager.addressFactory.createSipURI(nvrId, to);
        Address toNameAddress = sipManager.addressFactory.createAddress(toAddress);
        // toNameAddress.setDisplayName(targetName);
        ToHeader toHeader = sipManager.headerFactory.createToHeader(toNameAddress, null);

//        SipURI requestURI = sipManager.addressFactory.createSipURI("server",
//                DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());

        // URI requestURI = sipManager.addressFactory.createURI(to);

//        DeviceImpl.getInstance().getSipProfile().getRemoteSipNum()
//        URI requestURI = sipManager.addressFactory.createURI("sip:" + "34020000001180000001"
        URI requestURI = sipManager.addressFactory.createURI("sip:" + nvrId
                + "@" + to);
        // requestURI.setTransportParam("udp");

        ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

        CallIdHeader callIdHeader = sipManager.sipProvider.getNewCallId();

        CSeqHeader cSeqHeader = sipManager.headerFactory.createCSeqHeader(50l,
                Request.MESSAGE);

        MaxForwardsHeader maxForwards = sipManager.headerFactory
                .createMaxForwardsHeader(70);

        Request request = sipManager.messageFactory.createRequest(requestURI,
                Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);
        ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                .createContentTypeHeader("Application", "MANSCDP+xml");
        request.setContent(message, contentTypeHeader);
        System.out.println(request.toString());
        return request;
    }


    public Request buildHeart(MySipManager sipManager, String targetName, String to, String message) throws ParseException, InvalidArgumentException {

        /*from header*/
        SipURI from = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(), DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
        Address fromNameAddress = sipManager.addressFactory.createAddress(from);
        fromNameAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
        FromHeader fromHeader = sipManager.headerFactory.createFromHeader(fromNameAddress,
                "fh_Gb281818");

        /*to header 对象的地址*/
        URI toAddress = sipManager.addressFactory.createURI("sip:" + to);
        //URI toAddress = sipManager.addressFactory.createSipURI(targetName, to);
        Address toNameAddress = sipManager.addressFactory.createAddress(toAddress);
        // toNameAddress.setDisplayName(targetName);
        ToHeader toHeader = sipManager.headerFactory.createToHeader(toNameAddress, null);

//        SipURI requestURI = sipManager.addressFactory.createSipURI("server",
//                DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());

        // URI requestURI = sipManager.addressFactory.createURI(to);
        URI requestURI = sipManager.addressFactory.createURI("sip:" + DeviceImpl.getInstance().getSipProfile().getRemoteSipNum() + "@" + to);
        // requestURI.setTransportParam("udp");

        ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

        CallIdHeader callIdHeader = sipManager.sipProvider.getNewCallId();

        CSeqHeader cSeqHeader = sipManager.headerFactory.createCSeqHeader(50l,
                Request.MESSAGE);

        MaxForwardsHeader maxForwards = sipManager.headerFactory
                .createMaxForwardsHeader(70);

        Request request = sipManager.messageFactory.createRequest(requestURI,
                Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);
        ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                .createContentTypeHeader("Application", "MANSCDP+xml");
        request.setContent(message, contentTypeHeader);
        System.out.println(request.toString());
        return request;
    }


    public Request buildCatelogMsg(MySipManager sipManager, String targetName, String toaddress, String message) throws ParseException, InvalidArgumentException {
        Log.e("gaozy", DeviceImpl.getInstance().getSipProfile().getSipUserName());
        /*from header*/
        SipURI from = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getMySipNum(), DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
        Address fromNameAddress = sipManager.addressFactory.createAddress(from);
        //fromNameAddress.setDisplayName(DeviceImpl.getInstance().getSipProfile().getSipUserName());
        FromHeader fromHeader = sipManager.headerFactory.createFromHeader(fromNameAddress,
                "fh_Gb281818");

        /*to header 对象的地址*/
        SipURI to = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getRemoteSipNum(), DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());

        //URI toAddress = sipManager.addressFactory.createURI("sip:" + to);
        //URI toAddress = sipManager.addressFactory.createSipURI(targetName, to);
        Address toNameAddress = sipManager.addressFactory.createAddress(to);
        // toNameAddress.setDisplayName(targetName);
        ToHeader toHeader = sipManager.headerFactory.createToHeader(toNameAddress, null);

//        SipURI requestURI = sipManager.addressFactory.createSipURI("server",
//                DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());

        // URI requestURI = sipManager.addressFactory.createURI(to);
        URI requestURI = sipManager.addressFactory.createURI("sip:" + toaddress);
        // requestURI.setTransportParam("udp");

        ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

        CallIdHeader callIdHeader = sipManager.sipProvider.getNewCallId();

        CSeqHeader cSeqHeader = sipManager.headerFactory.createCSeqHeader(50l,
                Request.MESSAGE);

        MaxForwardsHeader maxForwards = sipManager.headerFactory
                .createMaxForwardsHeader(70);

        Request request = sipManager.messageFactory.createRequest(requestURI,
                Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);
        ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                .createContentTypeHeader("Application", "MANSCDP+xml");
        request.setContent(message, contentTypeHeader);
        System.out.println(request.toString());
        return request;
    }


    public Request makeInviteRequest(MySipManager sipManager, String to, int port, String nvrSipId,
                                     String ipcSipId, boolean isNvr) throws ParseException, InvalidArgumentException {
        String requestId = isNvr ? nvrSipId : ipcSipId;
        /*from header*/
        SipURI from = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(),
                DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
        Address fromNameAddress = sipManager.addressFactory.createAddress(from);
        FromHeader fromHeader = sipManager.headerFactory.createFromHeader(fromNameAddress, "fh_Gb281818");

        /*to header 对象的地址*/
        SipURI toAddress = sipManager.addressFactory.createSipURI(requestId,
                DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());
        Address toNameAddress = sipManager.addressFactory.createAddress(toAddress);
        ToHeader toHeader = sipManager.headerFactory.createToHeader(toNameAddress, null);

        URI requestURI = sipManager.addressFactory.createURI("sip:" + requestId
                + "@" + to+":"+DeviceImpl.getInstance().getSipProfile().getRemotePort());

        ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

        CallIdHeader callIdHeader = sipManager.sipProvider.getNewCallId();

        CSeqHeader cSeqHeader = sipManager.headerFactory.createCSeqHeader(50l, Request.INVITE);

        ContactHeader contactHeader = sipManager.headerFactory.createContactHeader(sipManager.createContactAddress());

        MaxForwardsHeader maxForwards = sipManager.headerFactory.createMaxForwardsHeader(250);

        SubjectHeader subjectHeader = sipManager.headerFactory.createSubjectHeader(ipcSipId
                + ":00000001," + DeviceImpl.getInstance().getSipProfile().getMySipNum() + ":00000002");

        Request request = sipManager.messageFactory.createRequest(requestURI,
                Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);
        request.addHeader(subjectHeader);
        request.addHeader(contactHeader);
        String sdpData = "v=0\r\n"
                + "o=" + DeviceImpl.getInstance().getSipProfile().getMySipNum() + " 0 0 "
                + "IN IP4 " + DeviceImpl.getInstance().getSipProfile().getLocalIp() + "\r\n"
                + "s=Play \r\n"
                + "c=IN IP4 " + DeviceImpl.getInstance().getSipProfile().getLocalIp() + "\r\n"
                + "t=0 0\r\n"
                + "m=video " + port + " RTP/AVP 96 97 98\r\n"
                + "a=recvonly\r\n"
                + "a=rtpmap:96 PS/90000\r\n"
                + "a=rtpmap:97 MPEG4/90000\r\n"
                + "a=rtpmap:98 H264/90000\r\n"
                + "y=0200000001\r\n" + "f=\r\n";
        ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                .createContentTypeHeader("application", "sdp");

        request.setContent(sdpData, contentTypeHeader);
        System.out.println(request.toString());
        return request;
    }

    public Request makeBackInviteRequest(MySipManager sipManager, String to, int port, String nvrSipId,
                                     String ipcSipId,String startTime,String endTime) throws ParseException, InvalidArgumentException {
        String requestId = nvrSipId;
//        118nvr
//        132通道
//        ipcSipId = "34020000001320000001";
//        nvrSipId =  "34020000001180000001";

        /*from header*/
        SipURI from = sipManager.addressFactory.createSipURI(DeviceImpl.getInstance().getSipProfile().getSipUserName(),
                DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
        Address fromNameAddress = sipManager.addressFactory.createAddress(from);
        FromHeader fromHeader = sipManager.headerFactory.createFromHeader(fromNameAddress, "fh_Gb281818");

        /*to header 对象的地址*/
        SipURI toAddress = sipManager.addressFactory.createSipURI(requestId,
                DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint());
        Address toNameAddress = sipManager.addressFactory.createAddress(toAddress);
        ToHeader toHeader = sipManager.headerFactory.createToHeader(toNameAddress, null);

        URI requestURI = sipManager.addressFactory.createURI("sip:" + requestId
                + "@" + to+":"+DeviceImpl.getInstance().getSipProfile().getRemotePort());

        ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

        CallIdHeader callIdHeader = sipManager.sipProvider.getNewCallId();

        CSeqHeader cSeqHeader = sipManager.headerFactory.createCSeqHeader(50l, Request.INVITE);

        ContactHeader contactHeader = sipManager.headerFactory.createContactHeader(sipManager.createContactAddress());

        MaxForwardsHeader maxForwards = sipManager.headerFactory.createMaxForwardsHeader(250);

//        SubjectHeader subjectHeader = sipManager.headerFactory.createSubjectHeader(ipcSipId
        SubjectHeader subjectHeader = sipManager.headerFactory.createSubjectHeader(requestId
                + ":00000001," + DeviceImpl.getInstance().getSipProfile().getMySipNum() + ":00000002");
//        SubjectHeader subjectHeader = sipManager.headerFactory.createSubjectHeader(ipcSipId
//                + ":00000001," + requestId + ":00000002");

        Request request = sipManager.messageFactory.createRequest(requestURI,
                Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);
        request.addHeader(subjectHeader);
        request.addHeader(contactHeader);
        String sdpData = "v=0\r\n"
                + "o=" + DeviceImpl.getInstance().getSipProfile().getMySipNum() + " 0 0 "
//                + "o=" + requestId + " 0 0 "
                + "IN IP4 " + DeviceImpl.getInstance().getSipProfile().getLocalIp() + "\r\n"
                + "s=Playback \r\n"
//                + "u=34020000001320000001:3\r\n"
                + "u="+ipcSipId+":3\r\n"
                + "c=IN IP4 " + DeviceImpl.getInstance().getSipProfile().getLocalIp() + "\r\n"
                + "t="+startTime+ " "+endTime+"\r\n"
                + "m=video " + port + " RTP/AVP 96 98 97\r\n"
                + "a=recvonly\r\n"
                + "a=rtpmap:96 PS/90000\r\n"
                + "a=rtpmap:98 H264/90000\r\n"
                + "a=rtpmap:97 MPEG4/90000\r\n";
//                + "y=0200000001\r\n"
//                        + "f=\r\n";
        ContentTypeHeader contentTypeHeader = sipManager.headerFactory
                .createContentTypeHeader("application", "sdp");

        request.setContent(sdpData, contentTypeHeader);
        Log.e("222", "makeBackInviteRequest: "+ request.toString());
        return request;
    }

}
