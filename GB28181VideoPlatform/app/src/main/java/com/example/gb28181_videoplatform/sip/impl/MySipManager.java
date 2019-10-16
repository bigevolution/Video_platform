package com.example.gb28181_videoplatform.sip.impl;

import android.gov.nist.javax.sdp.SessionDescriptionImpl;
import android.gov.nist.javax.sdp.parser.SDPAnnounceParser;
import android.gov.nist.javax.sip.SipStackExt;
import android.gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import android.gov.nist.javax.sip.message.SIPMessage;
import android.javax.sdp.Attribute;
import android.javax.sdp.Media;
import android.javax.sdp.MediaDescription;
import android.javax.sdp.SdpFactory;
import android.javax.sdp.SessionDescription;
import android.javax.sip.ClientTransaction;
import android.javax.sip.Dialog;
import android.javax.sip.DialogState;
import android.javax.sip.DialogTerminatedEvent;
import android.javax.sip.IOExceptionEvent;
import android.javax.sip.InvalidArgumentException;
import android.javax.sip.ListeningPoint;
import android.javax.sip.ObjectInUseException;
import android.javax.sip.PeerUnavailableException;
import android.javax.sip.RequestEvent;
import android.javax.sip.ResponseEvent;
import android.javax.sip.ServerTransaction;
import android.javax.sip.SipException;
import android.javax.sip.SipFactory;
import android.javax.sip.SipListener;
import android.javax.sip.SipProvider;
import android.javax.sip.SipStack;
import android.javax.sip.TimeoutEvent;
import android.javax.sip.TransactionAlreadyExistsException;
import android.javax.sip.TransactionTerminatedEvent;
import android.javax.sip.address.Address;
import android.javax.sip.address.AddressFactory;
import android.javax.sip.header.CSeqHeader;
import android.javax.sip.header.CallIdHeader;
import android.javax.sip.header.ContactHeader;
import android.javax.sip.header.ContentTypeHeader;
import android.javax.sip.header.HeaderFactory;
import android.javax.sip.header.ToHeader;
import android.javax.sip.header.ViaHeader;
import android.javax.sip.message.MessageFactory;
import android.javax.sip.message.Request;
import android.javax.sip.message.Response;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;

import com.example.gb28181_videoplatform.sip.AccountManagerImpl;
import com.example.gb28181_videoplatform.sip.ISipManager;
import com.example.gb28181_videoplatform.sip.NotInitializedException;
import com.example.gb28181_videoplatform.sip.RtpAddressMsg;
import com.example.gb28181_videoplatform.sip.SdpParserEntity;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MySipManager implements SipListener, ISipManager {
    public static SipRequestMsgBuilder sipRequestBuilder = new SipRequestMsgBuilder();
    private static SipStack sipStack;
    private SipProfile sipProfile;
    public SipProvider sipProvider;
    public HeaderFactory headerFactory;
    public AddressFactory addressFactory;
    public MessageFactory messageFactory;
    public SipFactory sipFactory;
    private ListeningPoint udpListeningPoint;
    private Handler mUpdateHandler,timeHandler;
    private boolean initialized;
    private HashMap<String, String> customHeaders;
    private ClientTransaction currentClientTransaction;
    private ServerTransaction currentServerTransaction;
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
    private String heartCallId;
    public boolean isFirst = true;
    public boolean mIsReceivedAddressSeted;
    private String mReceivedAddress;
    public MySipManager(SipProfile sipProfile) {
        this.sipProfile = sipProfile;
        initializeSipStack();
    }
    String content = "<?xml version=\"1.0\"?>"+
            "<Response>"+
            "<RecordList Num=\"2\">"+
            "<Item>" +
            "<DeviceID>64010000001310000001</DeviceID>" +
            "<Name>Camera1</Name>" +
            "<FilePath>64010000002100000001</FilePath>" +
            "<Address>Address1</Address>" +
            "<StartTime>2010-11-12T10:10:00</StartTime>" +
            "<EndTime>2010-11-12T10:20:00</EndTime>" +
            "<Secrecy>0</Secrecy>" +
            "<Type>time</Type>" +
            "<RecorderID>64010000003000000001</RecorderID>" +
            "</Item>"+
            "<Item>"+
            "<DeviceID>64010000001310000001</DeviceID>"+
            "<Name>Camera1</Name>"+
            "<FilePath>64010000002100000001</FilePath>"+
            "<Address>Address1</Address>"+
            "<StartTime>2019-11-12T10 : 20 : 00</StartTime>"+
            "<EndTime>2019-11-12T10 : 30 : 00</EndTime>"+
            "<Secrecy>0</Secrecy>"+
            "<Type>time</Type>"+
            "<RecorderID>64010000003000000001</RecorderID>"+
            "</Item>"+
            "</RecordList>"+
            "</Response>";

    /**
     * 初始化协议栈
     *
     * @return
     */
    private boolean initializeSipStack() {
        sipFactory = SipFactory.getInstance();
        sipFactory.resetFactory();
        sipFactory.setPathName("android.gov.nist");

        Properties properties = new Properties();
        properties.setProperty("android.javax.sip.OUTBOUND_PROXY", DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint()
                + "/" + DeviceImpl.getInstance().getSipProfile().getTransport());
        properties.setProperty("android.javax.sip.STACK_NAME", "androidSip");

        try {
            if (udpListeningPoint != null) {
                // Binding again
                sipStack.deleteListeningPoint(udpListeningPoint);
                sipProvider.removeSipListener(this);
            }
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("createSipStack " + sipStack);
        } catch (PeerUnavailableException e) {
            return false;
        } catch (ObjectInUseException e) {
            return false;
        }

        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            //创建一对SipProvider  和 ListeningPoint 对象。这两个对象提供发送和接收消息的通信功能
            udpListeningPoint = sipStack.createListeningPoint(
                    DeviceImpl.getInstance().getSipProfile().getLocalIp(), DeviceImpl.getInstance().getSipProfile().getLocalPort(), DeviceImpl.getInstance().getSipProfile().getTransport());
            sipProvider = sipStack.createSipProvider(udpListeningPoint);
            sipProvider.addSipListener(this);// SIP 消息的监听器
            initialized = true;
            Log.e("gaozy", "成功初始化sip协议栈");
        } catch (PeerUnavailableException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Handler getmUpdateHandler() {
        return mUpdateHandler;
    }

    public void setmUpdateHandler(Handler mUpdateHandler) {
        this.mUpdateHandler = mUpdateHandler;
    }
    public Handler getmTimeHandler() {
        return timeHandler;
    }

    public void setTimeHandler(Handler timeHandler) {
        this.timeHandler = timeHandler;
    }

    public HashMap<String, String> getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(HashMap<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    /**
     * 接收请求
     *
     * @param requestReceivedEvent
     */
    @Override
    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransaction = requestReceivedEvent
                .getServerTransaction();
        Log.e("gaozy_processRequest", " \n" + requestReceivedEvent.getRequest().toString());
        System.out.println("\n\nRequest " + request.getMethod()
                + " received at " + sipStack.getStackName()
                + " with server transaction id " + serverTransaction);
        if (request.getMethod().equals(Request.INVITE)) {
            processInvite(requestReceivedEvent, serverTransaction);
        } else if (request.getMethod().equals(Request.ACK)) {
//             processAck(requestReceivedEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.BYE)) {
            processBye(requestReceivedEvent, serverTransaction);
        } else if (request.getMethod().equals(Request.CANCEL)) {
            // processCancel(requestReceivedEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.MESSAGE)) {
            processMessage(requestReceivedEvent, serverTransaction);
        }
    }

    /**
     * 接收响应
     *
     * @param responseEvent
     */
    @Override
    public void processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        ClientTransaction tid = responseEvent.getClientTransaction();
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
        if(tid==null){
            System.out.println("*********tid==null*******");
        }
        //System.out.println(tid.getRequest().toString());
        //System.out.println(tid.getRequest().getContent());
        //System.out.println("Dialog = " + tid.getDialog());
        //System.out.println("Dialog State is " + tid.getDialog().getState());
        //Log.e("gaozy_processResponse", response.getReasonPhrase());
        Log.e("Response-gpc", " \n" + response.toString());
        CallIdHeader callIdHeader = (CallIdHeader) response.getHeader(CallIdHeader.NAME);
        String responseCallID = callIdHeader.getCallId();
        Log.e("Response", "ResponseCallID======" + responseCallID);
       if (response.getStatusCode() == Response.OK) {//200
            if (cseq.getMethod().equals(Request.REGISTER)) {
                ContentTypeHeader contentHeader = (ContentTypeHeader) response.getHeader("Content-Type");
                // System.out.println("the type of ok response is:" + contentHeader);
                Message msg = new Message();
                if (registerState == 0) {
                    msg.what = 1;
                } else {
                    msg.what = 2;
                }
                mUpdateHandler.sendMessage(msg);
            } else if (cseq.getMethod().equals(Request.INVITE)) {//推流端ipc不需要
                System.out.println("Dialog after 200 OK  " + dialog);
                System.out.println("Dialog State after 200 OK  " + dialog.getState());
                Request ackRequest = null;
                try {
                    if (tid != null) {
                        dialog = tid.getDialog();
                    }
                    ackRequest = dialog.createAck(((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getSeqNumber());
                    System.out.println("Sending ACK");
                    dialog.sendAck(ackRequest);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                } catch (SipException e) {
                    e.printStackTrace();
                }
                // JvB: test REFER, reported bug in tag handling
                // dialog.sendRequest(  sipProvider.getNewClientTransaction( dialog.createRequest("REFER") ));
            } else if (cseq.getMethod().equals(Request.CANCEL)) {
                if (dialog.getState() == DialogState.CONFIRMED) {
                    // oops cancel went in too late. Need to hang up the
                    // dialog.
                    System.out
                            .println("Sending BYE -- cancel went in too late !!");
                    Request byeRequest = null;
                    try {
                        byeRequest = dialog.createRequest(Request.BYE);
                        ClientTransaction ct = sipProvider
                                .getNewClientTransaction(byeRequest);
                        dialog.sendRequest(ct);
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            } else if (cseq.getMethod().equals(Request.MESSAGE)) {
                if (heartCallId.endsWith(responseCallID)) {
                    //心跳正常
                    Log.e("gaozy", response.getStatusCode() + " " + response.getReasonPhrase());
                    Message msg = new Message();
                    msg.what = 6;
                    mUpdateHandler.sendMessage(msg);

                }
            }
        } else {//异常
            if (cseq.getMethod().equals(Request.MESSAGE)) {
                if (heartCallId.endsWith(responseCallID)) {
                    //心跳异常
                    Log.e("gaozy", response.getStatusCode() + " " + response.getReasonPhrase());
                    Message msg = new Message();
                    msg.what = 5;
                    msg.obj = response.getStatusCode() + " " + response.getReasonPhrase();
                    mUpdateHandler.sendMessage(msg);
                }
            } else if (cseq.getMethod().equals(Request.REGISTER)) {
                if (isFirst) {
                    ViaHeader viaHeader = (ViaHeader) response.getHeader(ViaHeader.NAME);
                    mReceivedAddress = viaHeader.getReceived();
                    if (mReceivedAddress != null) {
                        mIsReceivedAddressSeted = true;
                    }
                    isFirst = false;
                    Register(0);
                    return;
                }
                if (response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED
                        || response.getStatusCode() == Response.UNAUTHORIZED) {//鉴权
                    AuthenticationHelper authenticationHelper = ((SipStackExt) sipStack)
                            .getAuthenticationHelper(new AccountManagerImpl(),
                                    headerFactory);
                    try {
                        if (tid != null) {
                            ClientTransaction inviteTid = authenticationHelper
                                    .handleChallenge(response, tid, sipProvider, 5, true);
                            System.out.println(inviteTid.getRequest().toString());
                            inviteTid.sendRequest();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                } else {//注册失败
                    Message msg = new Message();
                    msg.what = 4;
                    msg.obj = response.getStatusCode() + " " + response.getReasonPhrase();
                    mUpdateHandler.sendMessage(msg);
                }
            }
        }
    }

    private void processInvite(RequestEvent requestReceivedEvent, ServerTransaction serverTransaction) {
        Request request = requestReceivedEvent.getRequest();
        ContentTypeHeader contentHeader = (ContentTypeHeader) request.getHeader("Content-Type");
        String contentType = contentHeader.getContentType();
        Log.e("gaozy", "contentType=====" + contentType);
        /**
         * 获取SDP消息，提取其中的媒体服务器的地址和端口号
         */
        String sdpFields = "";
        String ip = "";
        int port = 0;
        String psvalue = "";
        if (requestReceivedEvent.getRequest().getContent() != null) {
            sdpFields = new String(requestReceivedEvent.getRequest().getRawContent());
            Log.e("gaozy", sdpFields);
        }
        SdpParserEntity urlEntity = SdpParserEntity.parse(sdpFields);
        Map<String, String> params = urlEntity.getParams();
        String Yssrc = params.get("y");//SDP字段中的SSRC值
        Log.e("gaozy", "yyyyyyyyyyy================" + params.get("y") + "ffff==========" + params.get("f"));
        sdpFields = SdpParserEntity.getnewSdp();
        SDPAnnounceParser parser = new SDPAnnounceParser(sdpFields);
        try {
            SessionDescriptionImpl sessiondescription = parser.parse();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SdpFactory sdpFactory = SdpFactory.getInstance();
        SessionDescription sessionDescription;
        try {
            sessionDescription = sdpFactory
                    .createSessionDescription(sdpFields);
            String sname = sessionDescription.getSessionName().getValue();
            Log.e("gaozy", "sessionName====" + sname);
            ip = sessionDescription.getConnection().getAddress();
            Log.e("gaozy", "视频推送地址====" + ip);
            Vector mediaDescriptions = sessionDescription
                    .getMediaDescriptions(true);
            Log.e("gaozy", "mediaDescriptions.size() ===" + mediaDescriptions.size() + "");
            for (int i = 0; i < mediaDescriptions.size(); i++) {
                MediaDescription mediaDes = (MediaDescription) mediaDescriptions
                        .elementAt(i);
                System.out.println("m = " + mediaDes.toString());
                Media media = mediaDes.getMedia();
                Vector formats = media.getMediaFormats(false);
                //Log.e("gaozy", media.getMediaPort() + "////" + media.getProtocol() + "////" + media.getMediaType() + "./////");
                Log.e("gaozy", "视频推送端口====" + media.getMediaPort());
                port = media.getMediaPort();
                System.out.println("formats = " + formats);
                Vector attributes = mediaDes.getAttributes(true);
                for (int j = 0; j < attributes.size(); j++) {
                    Attribute attribute = (Attribute) attributes.elementAt(j);
                    String name = attribute.getName();
                    String value = attribute.getValue();
                    if (value != null && value.contains("PS")) {
                        psvalue = value.substring(0, 2);
                        Log.e("gaozy", "PS值====" + psvalue);
                    }
                }
            }
            RtpAddressMsg rtpAddressMsg = new RtpAddressMsg(ip, port, Yssrc);
            Message msg = new Message();
            msg.what = 3;
            msg.obj = rtpAddressMsg;
            mUpdateHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         *  First, send 100 TRING
         */
        try {
            System.out.println("First, send 100 TRING...");
            Log.e("222", "processInvite:----------------------------------------------- ");
            Response TringResponse = messageFactory.createResponse(Response.TRYING, request);
            if (serverTransaction == null) {
                serverTransaction = sipProvider.getNewServerTransaction(request);
            }
            serverTransaction.sendResponse(TringResponse);
            System.out.println(TringResponse.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         *  sencond, 下级平台发送sdp消息，告诉服务器自己推流的ip和port等信息
         */
        AcceptCall(serverTransaction, 8000, Yssrc);
    }

    public void AcceptCall(final ServerTransaction serverTransaction, final int port, final String ySSrc) {
        System.out.println("sencond，告诉服务器自己推流的ip和port等信息");
        Thread thread = new Thread() {
            public void run() {
                try {
                    SIPMessage sm = (SIPMessage) serverTransaction.getRequest();
                    Response responseOK = null;
                    try {
                        responseOK = messageFactory.createResponse(
                                Response.OK, serverTransaction.getRequest());
                        Address address = createContactAddress();
                        ContactHeader contactHeader = headerFactory.createContactHeader(address);
                        responseOK.addHeader(contactHeader);
                        ToHeader toHeader = (ToHeader) responseOK.getHeader(ToHeader.NAME);
                        toHeader.setTag("4321"); // Application is supposed to set.
                        responseOK.addHeader(contactHeader);
                        String Routeaddress = DeviceImpl.getInstance().getSipProfile().getLocalIp();
                        if (mIsReceivedAddressSeted) {
                            Routeaddress = mReceivedAddress;
                        }
                        String sdpData = "v=0\r\n"
                                + "o=" + sipProfile.getMySipNum() + " 0 0 "
                                + "IN IP4 " + Routeaddress + "\r\n"
                                + "s=Play \r\n"
                                + "c=IN IP4 "
                                + Routeaddress + "\r\n" + "t=0 0\r\n"
                                + "m=video " + port
                                /*+ " RTP/AVP 98\r\n"
                                + "a=rtpmap:98 H264/90000\r\n"*/
                                + " RTP/AVP 96\r\n"
                                + "a=rtpmap:96 PS/90000\r\n"
                                + "a=sendonly\r\n"
                                //+ "a=setup:active\n"
                                + "a=streamprofile:0\n"
                                //  + "m=audio 6018 RTP/AVP 100\r\n"
                                //  + "a=rtpmap:100 G711u\r\n"
                                //  + "a=username:admin\r\n" + "a=password:123456\r\n"
                                //v/0/0/0/0/0a/0/0/0   v/2/1/0/1/0a///
                                + "y=" + ySSrc + "\r\n" + "f=\r\n";
                        byte[] contents = sdpData.getBytes();

                        ContentTypeHeader contentTypeHeader =
                                headerFactory.createContentTypeHeader("application", "sdp");
                        responseOK.setContent(contents, contentTypeHeader);
                        serverTransaction.sendResponse(responseOK);
                        System.out.println(responseOK.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } catch (SipException e) {
                    e.printStackTrace();
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
    public void AcceptBackCall(final ServerTransaction serverTransaction, final int port, final String ySSrc) {
        System.out.println("sencond，告诉服务器自己推流的ip和port等信息");
        Thread thread = new Thread() {
            public void run() {
                try {
                    SIPMessage sm = (SIPMessage) serverTransaction.getRequest();
                    Response responseOK = null;
                    try {
                        responseOK = messageFactory.createResponse(
                                Response.OK, serverTransaction.getRequest());
                        Address address = createContactAddress();
                        ContactHeader contactHeader = headerFactory.createContactHeader(address);
                        responseOK.addHeader(contactHeader);
                        ToHeader toHeader = (ToHeader) responseOK.getHeader(ToHeader.NAME);
                        toHeader.setTag("4321"); // Application is supposed to set.
                        responseOK.addHeader(contactHeader);
                        String Routeaddress = DeviceImpl.getInstance().getSipProfile().getLocalIp();
                        if (mIsReceivedAddressSeted) {
                            Routeaddress = mReceivedAddress;
                        }
                        String sdpData = "v=0\r\n"
                                + "o=" + sipProfile.getMySipNum() + " 0 0 " + "IN IP4 " + Routeaddress + "\r\n"
                                + "s=Embedded Net DVR \r\n"
                                + "c=IN IP4 " + Routeaddress + "\r\n"
                                + "t=0 0\r\n"
                                + "m=video " + port + " RTP/AVP 96\r\n"
                                + "a=sendonly\r\n"
                                + "a=rtpmap:96 PS/90000\r\n"
                                //+ "a=setup:active\n"
                                + "a=streamprofile:0\n"
                                //  + "m=audio 6018 RTP/AVP 100\r\n"
                                //  + "a=rtpmap:100 G711u\r\n"
                                //  + "a=username:admin\r\n" + "a=password:123456\r\n"
                                //v/0/0/0/0/0a/0/0/0   v/2/1/0/1/0a///
                                + "y=" + ySSrc + "\r\n" + "f=\r\n";
                        byte[] contents = sdpData.getBytes();

                        ContentTypeHeader contentTypeHeader =
                                headerFactory.createContentTypeHeader("application", "sdp");
                        responseOK.setContent(contents, contentTypeHeader);
                        serverTransaction.sendResponse(responseOK);
                        System.out.println(responseOK.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } catch (SipException e) {
                    e.printStackTrace();
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void processBye(RequestEvent requestEvent,
                           ServerTransaction serverTransactionId) {
        try {
            System.out.println("shootist:  got a bye .");
            if (serverTransactionId == null) {
                System.out.println("shootist:  null TID.");
                return;
            }
            Dialog dialog = serverTransactionId.getDialog();
            System.out.println("Dialog State = " + dialog.getState());
            Response response = messageFactory.createResponse(200, requestEvent.getRequest());
            serverTransactionId.sendResponse(response);
            System.out.println("shootist:  Sending OK.");
            System.out.println("Dialog State = " + dialog.getState());
            Message msg = new Message();
            msg.what = 7;
            mUpdateHandler.sendMessage(msg);
        } catch (Exception ex) {
            ex.printStackTrace();
            //junit.framework.TestCase.fail("Exit JVM");
        }
    }

    private void processMessage(RequestEvent requestEvent, ServerTransaction serverTransaction) {

        try {
            /**
             * 解析XML文件，获取SN （查询请求的序号）编号
             */
            Log.e("接收Message", "processMessage:----------------------------------------------- " );
            String xmlString = null;
            if (requestEvent.getRequest().getContent() != null) {
//                Object obj = requestEvent.getRequest().getContent();
//                byte[] s = (byte[]) obj;
                xmlString = new String(requestEvent.getRequest().getRawContent());
            }
            XMLParse(xmlString);
            /**
             *  First, send 200 OK
             */
            Request prack = requestEvent.getRequest();
            Response prackOk = messageFactory.createResponse(200, prack);
            if (serverTransaction == null) {
                serverTransaction = sipProvider.getNewServerTransaction(requestEvent.getRequest());

            }
            //dialog = serverTransaction.getDialog();
            serverTransaction.sendResponse(prackOk);
            System.out.println(prackOk.toString());

            /**
             *  Second, 发送设备目录消息，跟查询请求的序号SN要保持一致。
             */
//            Log.e("解析SN", "SN = : " + SN + "nowSN = " + nowSN);
            if (CmdType.endsWith("Catalog")) {
                sendCatalogMsg(SN);
                //sendDevicrInfoMsg(SN);
            } else if (CmdType.endsWith("DeviceInfo")) {
                sendDevicrInfoMsg(SN);
                //TODO_SENDHEARTMESSAGE;
            }else if (CmdType.endsWith("RecordInfo")){
                //获得返回的数据，然后解析
                Message msg = new Message();
                msg.what = 1;
                msg.obj = xmlString;
                timeHandler.sendMessage(msg);
            }
            /**
             * Send a 200 OK response to complete the 3 way handshake for the
             * INIVTE.
             */
//            Response response = messageFactory.createResponse(200,
//                    inviteRequest);
//            ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
//            to.setTag(this.toTag);
//            Address address = addressFactory.createAddress("Shootme <sip:"
//                    + myAddress + ":" + myPort + ">");
//            ContactHeader contactHeader = headerFactory
//                    .createContactHeader(address);
//            response.addHeader(contactHeader);
//            inviteTid.sendResponse(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * CmdType == Catalog: 查询设备目录，KeepLive:心跳保活，DeviceControl:云台控制，RecordInfo:录像查询
     * SN 查询请求的序号
     */
    private String CmdType;
    private String SN ;//查询请求的序号
//    private String nowSN = "0";

    //XML解析
    private void XMLParse(String xmlData) {
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            //2,初始化解析器，设置要解析的流数据，并设置编码方式
            xmlPullParser.setInput(new StringReader(xmlData));
            //3,循环解析
            int type = xmlPullParser.getEventType();
            while (type != XmlPullParser.END_DOCUMENT) {
                //如果是开始标签
                if (type == XmlPullParser.START_TAG) {
                    if ("CmdType".equals(xmlPullParser.getName())) {
                        //获取文本数据
                        CmdType = xmlPullParser.nextText();
                        Log.e("gaozy", "CmdType===" + CmdType);
                    } else if ("SN".equals(xmlPullParser.getName())) {
//                        nowSN = xmlPullParser.nextText();
//                        if(Integer.parseInt(nowSN) > Integer.parseInt(SN)) {
//                            SN = nowSN;//获取文本数据
//                        }
//                        Log.e("解析SN", "nowSN:"+nowSN);
                        SN = xmlPullParser.nextText();
                    }
                }
                //让解析器移动到下一个
                type = xmlPullParser.next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {

    }

    @Override
    public void processIOException(IOExceptionEvent ioExceptionEvent) {

    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

    }

    public ArrayList<ViaHeader> createViaHeader() {
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader myViaHeader;
        try {
            myViaHeader = this.headerFactory.createViaHeader(
                    DeviceImpl.getInstance().getSipProfile().getLocalIp(), DeviceImpl.getInstance().getSipProfile().getLocalPort(),
                    DeviceImpl.getInstance().getSipProfile().getTransport(), null);
            myViaHeader.setRPort();
            myViaHeader.setBranch("z9hG4bK111998793" + System.currentTimeMillis());
            viaHeaders.add(myViaHeader);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return viaHeaders;
    }

    public Address createContactAddress() {

            try {
                if (mIsReceivedAddressSeted) {
                    return this.addressFactory.createAddress("sip:" + DeviceImpl.getInstance().getSipProfile().getMySipNum() + "@"
                            + mReceivedAddress + ":" + DeviceImpl.getInstance().getSipProfile().getLocalPort());
                } else {
                    return this.addressFactory.createAddress("sip:" + DeviceImpl.getInstance().getSipProfile().getMySipNum() + "@"
                            + DeviceImpl.getInstance().getSipProfile().getLocalEndpoint());
                }
//            return this.addressFactory.createAddress("sip:" + DeviceImpl.getInstance().getSipProfile().getMySipNum() + "@"
//                    + "192.168.5.59:5080");
            } catch (ParseException e) {
                return null;
            }

    }

    @Override
    public void SendMessage(String targetName, String deviceId, String to, String message) throws NotInitializedException {
        try {
            final Request  request = sipRequestBuilder.buildMessage(this, targetName, deviceId, to, message);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        ClientTransaction transaction = sipProvider.getNewClientTransaction(request);
                        transaction.sendRequest();
                        dialog = transaction.getDialog();
                    } catch (TransactionAlreadyExistsException e){
                        e.printStackTrace();
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            };
            fixedThreadPool.execute(runnable);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    public void SendTimeMsg(String deviceId, String nvrId, String to, String message) throws NotInitializedException {
        try {
            final Request  request = sipRequestBuilder.buildTimeMsg(this, deviceId, nvrId, to,message);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        ClientTransaction transaction = sipProvider.getNewClientTransaction(request);
                        transaction.sendRequest();
                        dialog = transaction.getDialog();
                    } catch (TransactionAlreadyExistsException e){
                        e.printStackTrace();
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            };
            fixedThreadPool.execute(runnable);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void SendDTMF(String digit) throws NotInitializedException {

    }

    private int registerState;
    private Dialog dialog;

    @Override
    public void Register(int state) {

        if (!initialized)
            return;//If initialization failed, don't proceeds
//		Register register =  new Register();
        registerState = state;
        try {
//			Request request = register.buildRegister(this);
            final Request request = sipRequestBuilder.buildRegister(this, state);
            // currentClientTransaction = this.sipProvider.getNewClientTransaction(request);

            // Send the request statefully, through the client transaction.
//            Thread thread = new Thread() {
//                public void run() {
//                    try {
//                        ClientTransaction transaction = sipProvider.getNewClientTransaction(request);
//                        transaction.sendRequest();
//                    } catch (SipException e) {
//                        e.printStackTrace();
//                    }
//                }
//            };
//            thread.start();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        ClientTransaction transaction = sipProvider.getNewClientTransaction(request);
                        transaction.sendRequest();
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            };
            fixedThreadPool.execute(runnable);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Call(String to, int localRtpPort) throws NotInitializedException {

    }

    @Override
    public void Hangup() throws NotInitializedException {

    }

    @Override
    public void SendHeart(String targetName, String to, String message) throws NotInitializedException {
        if (!initialized)
            throw new NotInitializedException("Sip Stack not initialized");
        try {
            final Request request = sipRequestBuilder.buildHeart(this, targetName, to, message);
            CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
            heartCallId= callIdHeader.getCallId();
            Log.e("gaozy", "RequestHeartCallID=========" + heartCallId);
//            class MyThread extends Thread {
//                private SipProvider sipProvider;
//                private Request request;
//
//                public MyThread(SipProvider sp, Request r) {
//                    this.sipProvider = sp;
//                    this.request = r;
//                }
//
//                public void run() {
//                    try {
//                        sipProvider.sendRequest(request);
//                    } catch (SipException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            MyThread thread = new MyThread(this.sipProvider, r);
//            thread.start();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        sipProvider.sendRequest(request);
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            };
            fixedThreadPool.execute(runnable);
        } catch (ParseException e1) {
            e1.printStackTrace();
        } catch (InvalidArgumentException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void SendBye() throws NotInitializedException {
//        try {
//            request = sipRequestBuilder.buildBye(this);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        } catch (InvalidArgumentException e) {
//            e.printStackTrace();
//        }
        if(dialog != null){
            try {
                final Request byeRequest = dialog.createRequest(Request.BYE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
                            dialog.sendRequest(ct);
                        } catch (TransactionAlreadyExistsException e){
                            e.printStackTrace();
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                };
                fixedThreadPool.execute(runnable);
            } catch (SipException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void invite(String to, int port, String nvrSipId, String ipcSipId, boolean isNvr) throws NotInitializedException {

        try {
            final Request  request = sipRequestBuilder.makeInviteRequest(this, to, port, nvrSipId, ipcSipId, isNvr);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        ClientTransaction transaction = sipProvider.getNewClientTransaction(request);
                        transaction.sendRequest();
                        dialog = transaction.getDialog();
                    } catch (TransactionAlreadyExistsException e){
                        e.printStackTrace();
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            };
            fixedThreadPool.execute(runnable);
//                        class MyThread extends Thread {
//                private SipProvider sipProvider;
//                private Request request;
//
//                public MyThread(SipProvider sp, Request r) {
//                    this.sipProvider = sp;
//                    this.request = r;
//                }
//
//                public void run() {
//                    try {
//                        sipProvider.sendRequest(request);
//                    } catch (SipException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            MyThread thread = new MyThread(this.sipProvider, request);
//            thread.start();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    public void backInvite(String to, int port, String nvrSipId, String ipcSipId,String startTime,String endTime) throws NotInitializedException {

        try {
            final Request  request = sipRequestBuilder.makeBackInviteRequest(this, to, port, nvrSipId, ipcSipId,startTime,endTime);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        ClientTransaction transaction = sipProvider.getNewClientTransaction(request);
                        transaction.sendRequest();
                        dialog = transaction.getDialog();
                    } catch (TransactionAlreadyExistsException e){
                        e.printStackTrace();
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            };
            fixedThreadPool.execute(runnable);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    private void sendCatalogMsg(String sn) {
        if (!initialized)
            return;
        //组建xml数据
        //组建xml数据
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\"?>");
        xml.append("\n");
        xml.append("<Response>");
        xml.append("\n");
        xml.append("<CmdType>Catalog</CmdType>");
        xml.append("\n");
        xml.append("<SN>" + sn + "</SN>");
        xml.append("\n");
        xml.append("<DeviceID>" + sipProfile.getMySipNum() + "</DeviceID>");
        xml.append("\n");
        xml.append("<SumNum>1</SumNum>");
        xml.append("\n");
        xml.append("<DeviceList>");
        xml.append("\n");
        xml.append("<Item>");
        xml.append("\n");
        xml.append("<DeviceID>" + DeviceImpl.getInstance().getSipProfile().getMySipNum() + "</DeviceID>");
        xml.append("\n");
        xml.append("<Name>Gaozy_Camera1</Name>");
        xml.append("\n");
        xml.append("<Manufacturer>fiberhome</Manufacturer>");
        xml.append("\n");
        xml.append("<Owner>" + "Owner" + "</Owner>");
        xml.append("\n");
        xml.append("<Model>1</Model>");
        xml.append("\n");
        xml.append("<CivilCode>" + DeviceImpl.getInstance().getSipProfile().getMySipNum() + "</CivilCode>");
        xml.append("\n");
        xml.append("<Address>" + DeviceImpl.getInstance().getSipProfile().getLocalIp() + "</Address>");
        xml.append("\n");
        xml.append("<Parental>0</Parental>");
        xml.append("\n");
        xml.append("<CertNum>1</CertNum>");
        xml.append("\n");
        xml.append("<Certifiable>1</Certifiable>");
        xml.append("\n");
        xml.append("<ErrCode>400</ErrCode>");
        xml.append("\n");
        xml.append("<ParentID>" + DeviceImpl.getInstance().getSipProfile().getMySipNum() + "</ParentID>");
        xml.append("\n");
        xml.append("<RegisterWay>1</RegisterWay>");
        xml.append("\n");
        xml.append("<Secrecy>1</Secrecy>");
        xml.append("\n");
        xml.append("<IPAddress>" + DeviceImpl.getInstance().getSipProfile().getLocalIp() + "</IPAddress>");
        xml.append("\n");
        xml.append("<Port>" + DeviceImpl.getInstance().getSipProfile().getLocalPort() + "</Port>");
        xml.append("\n");
        xml.append("<Status>ON</Status>");
        xml.append("\n");
        xml.append("</Item>");
        xml.append("\n");
        xml.append("</DeviceList>");
        xml.append("\n");
        xml.append("</Response>");

        try {
            Request request = sipRequestBuilder.buildCatelogMsg(this, "", sipProfile.getRemoteEndpoint(), xml.toString());
            sipProvider.sendRequest(request);
//            class MyThread extends Thread {
//                private SipProvider sipProvider;
//                private Request request;
//
//                public MyThread(SipProvider sp, Request r) {
//                    this.sipProvider = sp;
//                    this.request = r;
//                }
//
//                public void run() {
//                    try {
//                        sipProvider.sendRequest(request);
//                    } catch (SipException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            MyThread thread = new MyThread(this.sipProvider, r);
//            thread.start();
        } catch (ParseException e1) {
            e1.printStackTrace();
        } catch (InvalidArgumentException e1) {
            e1.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    private void sendDevicrInfoMsg(String sn) {
        if (!initialized)
            return;
        //组建xml数据
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\"?>");
        xml.append("\n");
        xml.append("<Response>");
        xml.append("\n");
        xml.append("<CmdType>DeviceInfo</CmdType>");
        xml.append("\n");
        xml.append("<SN>" + sn + "</SN>");
        xml.append("\n");
        xml.append("<DeviceID>" + sipProfile.getMySipNum() + "</DeviceID>");
        xml.append("\n");
        xml.append("<Result>OK</Result>");
        xml.append("\n");
        xml.append("<DeviceType>IP Camera</DeviceType>");
        xml.append("\n");
        xml.append("<Manufacturer>Hikvision</Manufacturer>");
        xml.append("\n");
        xml.append("<Model>S-IPC-T12H-I</Model>");
        xml.append("\n");
        xml.append("<Firmware>V5.5.80</Firmware>");
        xml.append("\n");
        xml.append("<Channel>1</Channel>");
        xml.append("\n");
        xml.append("</Response>");
        try {
            Request request = sipRequestBuilder.buildCatelogMsg(this, "", sipProfile.getRemoteIp(), xml.toString());
            sipProvider.sendRequest(request);
//            class MyThread extends Thread {
//                private SipProvider sipProvider;
//                private Request request;
//
//                public MyThread(SipProvider sp, Request r) {
//                    this.sipProvider = sp;
//                    this.request = r;
//                }
//
//                public void run() {
//                    try {
//                        sipProvider.sendRequest(request);
//                    } catch (SipException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            MyThread thread = new MyThread(this.sipProvider, r);
//            thread.start();
        } catch (ParseException e1) {
            e1.printStackTrace();
        } catch (InvalidArgumentException e1) {
            e1.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }



    public void Destroy() {
        sipStack.stop();
    }
}
