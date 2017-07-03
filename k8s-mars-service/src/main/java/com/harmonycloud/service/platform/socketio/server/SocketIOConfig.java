package com.harmonycloud.service.platform.socketio.server;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.harmonycloud.k8s.bean.ObjectReference;
//import com.harmonycloud.k8s.util.KubernatesHost;
import com.harmonycloud.service.application.impl.TerminalServiceImpl;
import com.harmonycloud.service.platform.socketio.message.Notification;
import com.harmonycloud.service.platform.socketio.message.Resize;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.sun.jna.Platform;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by andy on 17-2-23.
 */
@Service("socketIOConfig")
public class SocketIOConfig {

	private static SocketIOServer server;

	static Map<String, SocketIOClient> clientsMap = new HashMap<>();

	static Map<String, SocketIOClient> podClientsMap = new HashMap<>();

	@Value("#{propertiesReader['socketio.host']}")
	private String socketIOhost;
	@Value("#{propertiesReader['socketio.port']}")
	private Integer socketIOPort;
	@Value("#{propertiesReader['socketio.context']}")
	private String socketContext;

	private final static String NOTI = "noti";

	private final static String OPEN = "open";

	private final static String RESIZE = "resize";

	private final static String INPUT = "input";

	private  PtyProcess term = null;


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

//	public void startServer() throws InterruptedException {
//		server = new SocketIOServer(socketConfig());
//
//		//监听通知事件
//		server.addEventListener(INPUT, String.class, new DataListener<String>() {
//			@Override
//			public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws IOException, InterruptedException {
//
//				UUID sessionId = client.getSessionId();
//				SocketIOClient currentClient = podClientsMap.get(sessionId.toString());
//
//				if(null != currentClient){
//
//
//					//将写入到term
//					if( term != null){
//						OutputStream out = term.getOutputStream();
//						out.write(data.getBytes());
//					}
//					client.sendEvent("output",data+"\\7");
//
//					final CountDownLatch latch = new CountDownLatch(1);
//					final int[] result = {-1};
//
//					// Asynchronously wait for the process to end...
//					Thread t = new Thread() {
//						public void run() {
//							try {
//								Thread.sleep(2000);
//								Scanner s = new Scanner(term.getInputStream(), "utf-8");
//								while (s.hasNextLine()) {
//									System.out.println(s.nextLine());
//									currentClient.sendEvent("output", s.nextLine());
//								}
//								term.getInputStream().close();
//								result[0] = term.waitFor();
//								System.out.println(result[0]);
////								while (result[0] == -1) {
////									TimeUnit.MILLISECONDS.sleep(100L);
////									result[0] = term.waitFor();
////								}
//							} catch (InterruptedException e) {
//								// Simply stop the thread...
//							} catch (IOException e) {
//									e.printStackTrace();
//							} finally {
//								latch.countDown();
//							}
//						}
//					};
//					t.start();
//					latch.await(1000, TimeUnit.MILLISECONDS);
////					latch.await();
////                    t.join();
//				}
//            }
//		});
//
//		server.addEventListener(RESIZE, Resize.class, new DataListener<Resize>() {
//			@Override
//			public void onData(SocketIOClient client, Resize data, AckRequest ackRequest) throws IOException, InterruptedException {
//
//				UUID sessionId = client.getSessionId();
//				SocketIOClient currentClient = podClientsMap.get(sessionId.toString());
//
//				if(null != currentClient){
//
//					//将写入到term
//					if( term != null){
//
//						WinSize ws = new WinSize();
//						ws.ws_col = data.getCol();
//						ws.ws_row = data.getRow();
//						term.setWinSize(ws);
//					}
//
//				}
//			}
//		});
//
//		server.addConnectListener(getNotiConnect());
//
//		server.addDisconnectListener(getNotiDisConnect());
//
//		server.start();
//		Thread.sleep(Integer.MAX_VALUE);
//		server.stop();
//	}

//	public ConnectListener getNotiConnect(){
//		return client -> {
//			HandshakeData handshakeData = client.getHandshakeData();
//
//			//获取客户端连接的userName参数
//			String user = client.getHandshakeData().getSingleUrlParam("userName") ;
//			if(user != null){
//				clientsMap.put(user,client);
//				//给客户端发送消息
//				Notification notification = new Notification();
//				notification.setTitle("ddddsss");
//				notification.setType("message");
//				notification.setMessage("hehe");
//				ObjectReference ob = new ObjectReference();
//				ob.setName("lianjie");
//				notification.setTarget(ob);
//				client.sendEvent(NOTI,notification);
//
//				String open = handshakeData.getSingleUrlParam("terminal");
//				if(OPEN.equals(open)){
//					try {
////						term = PtyProcess.exec(new String[]{"/bin/sh","-l"});
//
//                        KubernatesHost kubernatesHost = new KubernatesHost();
//
//						String sn = client.getHandshakeData().getSingleUrlParam("sn");
//						Map<String, String> podData = TerminalServiceImpl.POD_DATA_LIST.get(sn);
//						String pod = podData.get("pod");
//						String container = podData.get("container");
//						String namespace = podData.get("namespace");
//
//						String protocol = kubernatesHost.getProtocol();
//						String host =  kubernatesHost.getHost();
//						String port =  kubernatesHost.getPort();
//                        String token = podData.get("token");;
//
//						String[] cmd = { "kubectl","exec",pod,"--container="+container,"--namespace="+namespace,"-it","bash",
//								"--server="+protocol+"://"+host+":"+port,"--token="+token,"--insecure-skip-tls-verify=true"};
//						term = PtyProcess.exec(cmd);
//
//						UUID sessionId = client.getSessionId();
//						podClientsMap.put(sessionId.toString(), client);
//						/*int result = term.waitFor();
//						System.out.println("result:"+result);*/
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			if(StringUtils.isEmpty(user) ){
//				server.stop();
//				server = null;
//			}
//
//		};
//	}

	public DisconnectListener getNotiDisConnect(){
		return client -> {
			HandshakeData handshakeData = client.getHandshakeData();
			String open = handshakeData.getSingleUrlParam("terminal");
			if(OPEN.equals(open)){
				if( term != null){
					term.destroy();
					UUID sessionId = client.getSessionId();
					podClientsMap.remove(sessionId.toString());
				}
			}
			String user = client.getHandshakeData().getSingleUrlParam("userName") ;
			if(StringUtils.isNotEmpty(user)){
				clientsMap.remove(user);
			}

			System.out.println("断开");
		};
	}


	/**
	 *  给所有连接客户端推送消息
	 * @param eventType 推送的事件类型
	 * @param message  推送的内容
	 */
	public void sendMessageToAllClient(String eventType,String message){
		Collection<SocketIOClient> clients = server.getAllClients();
		for(SocketIOClient client: clients){
			client.sendEvent(eventType,message);
		}
	}

	/**
	 * 给某个用户推送消息
	 * @param userName 用户名称
	 * @param eventType 推送事件类型
	 * @param message 推送的消息内容
	 */
	public void sendMessageToOneClient(String userName,String eventType,Object message){
		try {
			if(userName != null && !"".equals(userName)){
				SocketIOClient client = (SocketIOClient)clientsMap.get(userName);
				if(client != null){
					client.sendEvent(eventType,message);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String[] preparePingCommand(int count) {
		String value = Integer.toString(count);
		if (Platform.isWindows()) {
			return new String[]{"ping", "-n", value, "127.0.0.1"};
		} else if (Platform.isSolaris()) {
			return new String[]{"/usr/sbin/ping", "-s", "127.0.0.1", "64", value};
		} else if (Platform.isMac() || Platform.isFreeBSD() || Platform.isOpenBSD()) {
			return new String[]{"/sbin/ping", "-c", value, "127.0.0.1"};
		} else if (Platform.isLinux()) {
			return new String[]{"/bin/ping", "-c", value, "127.0.0.1"};
		}

		throw new RuntimeException("Unsupported platform!");
	}
}
