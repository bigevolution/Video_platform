package com.example.gb28181_videoplatform.netty.serialiaztion;

import com.example.gb28181_videoplatform.netty.util.SerializationUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public class ProtobufMsgEncoder extends  MessageToByteEncoder {
	private Class<?> genericClass;

	public ProtobufMsgEncoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}
    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
    	  if (genericClass.isInstance(in)) {
            byte[] data = SerializationUtil.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
    	}
    }
}
