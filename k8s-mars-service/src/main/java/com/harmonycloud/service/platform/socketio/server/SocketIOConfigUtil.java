package com.harmonycloud.service.platform.socketio.server;
/*
package com.harmonycloud.platform.socketio.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.ObjectReference;
import com.harmonycloud.platform.socketio.enums.NamespaceEnum;
import com.harmonycloud.platform.socketio.message.Notification;

*/
/**
 * Created by andy on 17-2-23.
 *//*

//@Service("socketIOConfig")
public class SocketIOConfigUtil {

	private static SocketIOServer server;

	static private Map<String, SocketIOClient> clientsMap = new HashMap<>();

	@Value("#{propertiesReader['socketio.host']}")
	private String socketIOhost;
	@Value("#{propertiesReader['socketio.port']}")
	private Integer socketIOPort;
	@Value("#{propertiesReader['socketio.context']}")
	private String socketContext;

	private final static String NOTI = "noti";

	
	public Configuration socketConfig() {
		Configuration socketIOConfig = new Configuration();
		socketIOConfig.setHostname(socketIOhost);
		socketIOConfig.setPort(socketIOPort);
		socketIOConfig.setContext(socketContext);

		return socketIOConfig;
	}

	public static SocketIOServer getServer() {
		return server;
	}

	public void startServer() throws InterruptedException {
		server = new SocketIOServer(socketConfig());
		SocketIONamespace notiSocketNamespace = server.addNamespace(NamespaceEnum.NOTINAMESPACE.getNamespace());
		SocketIONamespace wettySocketNamespace = server.addNamespace(NamespaceEnum.WETTYNAMESPACE.getNamespace());

		// 
		notiSocketNamespace.addConnectListener(getNotiConnect());
		server.start();
		//监听广告推送事件，noti为事件名称，自定义
		server.addEventListener(NOTI, String.class, new DataListener<String>(){
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws ClassNotFoundException {

				String sa = client.getRemoteAddress().toString();
				String clientIp = sa.substring(1,sa.indexOf(":"));//获取客户端连接的ip
				Map params = client.getHandshakeData().getUrlParams();//获取客户端url参数

				System.out.println(clientIp+"：客户端：************"+data);
			}
		});

		//添加客户端连接事件
		server.addConnectListener(new ConnectListener() {
			@Override
			public void onConnect(SocketIOClient client) {
				//获取客户端连接的userName参数
				String user = client.getHandshakeData().getSingleUrlParam("userName") ;
				if(user != null){
					clientsMap.put(user,client);
					//给客户端发送消息
					Notification notification = new Notification();
					notification.setTitle("ddddsss");
					notification.setType("message");
					notification.setMessage("hehe");
					ObjectReference ob = new ObjectReference();
					ob.setName("lianjie");
					notification.setTarget(ob);
					*/
/*client.sendEvent(NOTI,notification);
					client.sendEvent(NOTI, JsonUtil.convertToJson(notification));*//*

					notiSocketNamespace.getBroadcastOperations().sendEvent(NOTI,notification);
				}
				if(StringUtils.isEmpty(user)){
					server.stop();
					server = null;
				}
			}
		});
		//添加客户端断开连接事件
		server.addDisconnectListener(new DisconnectListener(){
			@Override
			public void onDisconnect(SocketIOClient client) {
				String user = client.getHandshakeData().getSingleUrlParam("userName") ;

				if(StringUtils.isNotEmpty(user)){
					clientsMap.remove(user);
				}
			}
		});

		Thread.sleep(Integer.MAX_VALUE);
		server.stop();
	}

	private ConnectListener getNotiConnect(){
		return client -> {
			HandshakeData handshakeData = client.getHandshakeData();
			//获取客户端连接的userName参数
			String user = client.getHandshakeData().getSingleUrlParam("userName") ;
			if(user != null){
				clientsMap.put(user,client);
				//给客户端发送消息
				Notification notification = new Notification();
				notification.setTitle("ddddsss");
				notification.setType("message");
				notification.setMessage("hehe");
				ObjectReference ob = new ObjectReference();
				ob.setName("lianjie");
				notification.setTarget(ob);
				client.sendEvent(NOTI,notification);
				client.sendEvent(NOTI, JsonUtil.convertToJson(notification));
			}
			if(StringUtils.isEmpty(user)){
				server.stop();
				server = null;
			}
		};
	}
	*/
/**
	 *  给所有连接客户端推送消息
	 * @param eventType 推送的事件类型
	 * @param message  推送的内容
	 *//*

	public void sendMessageToAllClient(String eventType,String message){
		Collection<SocketIOClient> clients = server.getAllClients();
		for(SocketIOClient client: clients){
			client.sendEvent(eventType,message);
		}
	}
	
	*/
/**
	 * 给某个用户推送消息
	 * @param userName 用户名称
	 * @param eventType 推送事件类型
	 * @param message 推送的消息内容
	 *//*

	public void sendMessageToOneClient(String userName,String eventType,Object message){
		try {
			if(userName != null && !"".equals(userName)){
				SocketIOClient client = (SocketIOClient)clientsMap.get(userName);
				if(client != null){
					client.sendEvent(eventType,message);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();    //==sonar leak==
		}
	}
}
*/
