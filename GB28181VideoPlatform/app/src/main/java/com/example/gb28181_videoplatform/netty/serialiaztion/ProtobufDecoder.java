package com.example.gb28181_videoplatform.netty.serialiaztion;


import com.example.gb28181_videoplatform.netty.util.SerializationUtil;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;


public class ProtobufDecoder extends ByteToMessageDecoder {
	private Class<?> genericClass;

	public ProtobufDecoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// TODO Auto-generated method stub
		
		  if (in.readableBytes() < 4) {
			  	//这个HEAD_LENGTH是我们用于表示头长度的字节数。  由于上面我们传的是一个int类型的值，所以这里HEAD_LENGTH的值为4.
	            return;
	        }
	        in.markReaderIndex(); //我们标记一下当前的readIndex的位置
	        int dataLength = in.readInt(); // 读取传送过来的消息的长度。ByteBuf 的readInt()方法会让他的readIndex增加4
	        if (dataLength < 0) {  // 我们读到的消息体长度为0，这是不应该出现的情况，这里出现这情况，关闭连接。
	            ctx.close();
	        }
	        if (in.readableBytes() < dataLength) { //读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex. 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
	            in.resetReaderIndex();
	            return;
	        }
	        byte[] data = new byte[dataLength];
	        //  嗯，这时候，我们读到的长度，满足我们的要求了，把传送过来的数据，取出来吧~~
	        in.readBytes(data);
	        Object obj = SerializationUtil.deserialize(data, genericClass);
	        out.add(obj);	
	}

}
