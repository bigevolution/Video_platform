package com.example.gb28181_videoplatform.netty.querier;

import android.util.Log;

import com.example.gb28181_videoplatform.netty.service.AbstractNews;
import com.example.gb28181_videoplatform.netty.util.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;


public final class Querier<T extends AbstractNews> {
	Logger mLog= LoggerFactory.getLogger(Querier.class);
	
    /**
     * 日志管理
     */

	private Map<String, List<T>> container = new ConcurrentHashMap<String, List<T>>(); // <requestId, callBack> ;

	private static class SingletonHolder {
		private static final Querier<AbstractNews> instance = new Querier<>();
	}

	private Querier() {
	}

	public static final Querier getInsatance() {
		return SingletonHolder.instance;
	}

	/**
	 * 根据消息类型查找它的实现类，
	 * @param ctx   消息连接
	 * @param req
	 */
	public void execute(ChannelHandlerContext ctx,Request req) {
		if(req.getType()==null||req.getType().isEmpty()) {
			Log.i("Querier","消息为空");
			return;
		}
		List<T> collection = container.get(req.getType());
		if(collection ==null ){
			mLog.error("unknown type="+req.getType()+",to string="+req.toString());
			return;
		}
		for (T element : collection) {
			try {
				element.run(ctx,req);
			} catch (Exception e) {
				e.printStackTrace();
				mLog.error("handle msg error:"+e.getMessage());
			}
		}
	}

	public void attach(String key, T element) {
		// collection.add(element);
		if (container.containsKey(key)) {
			container.get(key).add(element);
			return;
		}
		List<T> collection = new ArrayList<T>();
		collection.add(element);
		container.put(key, collection);
	}

}
