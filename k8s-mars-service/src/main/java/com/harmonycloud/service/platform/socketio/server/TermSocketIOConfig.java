package com.harmonycloud.service.platform.socketio.server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.harmonycloud.service.platform.bean.Size;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.SocketUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by andy on 17-2-23.
 */
@Service("termSocketIOConfig")
public class TermSocketIOConfig {

	static SocketIOServer server;

	static Map<String, SocketIOClient> clientsMap = new HashMap<>();

//    Process proc = null;

	private  PtyProcess term = null;

	@Value("#{propertiesReader['socketio.host']}")
	private String socketIOhost;
	@Value("#{propertiesReader['socketio.port']}")
	private Integer socketIOPort;
	@Value("#{propertiesReader['socketio.context']}")
	private String socketContext;

	private final static String RESIZE = "resize";
	private final static String INPUT = "input";
    final CountDownLatch latch = new CountDownLatch(1);

	public Configuration socketConfig() {
		Configuration socketIOConfig = new Configuration();
		socketIOConfig.setHostname(socketIOhost);
		socketIOConfig.setPort(socketIOPort);
		socketIOConfig.setContext("/rest/wetty");
		return socketIOConfig;
	}

	public SocketIOServer socketIOServer(){
		server = new SocketIOServer(socketConfig());
		return server;
	}

	public static SocketIOServer getServer() {
		return server;
	}

	public void startServer() throws InterruptedException {

		socketIOServer().start();
 		server.addEventListener(RESIZE, String.class, new DataListener<String>(){
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws ClassNotFoundException {
//				int col =  client.get("col");
//				int row =  client.get("row");
//				if (term != null){
//                    term.setWinSize(new WinSize(col,row));
//                }
				System.out.println(data);
			}
		});
		//监听通知事件
		server.addEventListener(INPUT, String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws IOException, InterruptedException {
                 //将写入到term
				if( term != null){
					OutputStream out = term.getOutputStream();
					out.write(data.getBytes());
 				}
				System.out.println(data);

                client.sendEvent("output",data);
                InputStream is = term.getInputStream();
                System.out.println("陈之谜"+is.read());
                Thread t1 = new Thread(){

                    public void run(){

                        int ch;
                        try {
                            while((ch = is.read())>0){
                                System.out.println(ch);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("chenzhimin");
                    }};
                t1.start();

            }
		});



		/**
		 * 监听其他事件
		 */

		//添加客户端连接事件
		server.addConnectListener(new ConnectListener() {
			@Override
			public void onConnect(SocketIOClient client) {
				//打开终端登录进入pod
				try {


					term = PtyProcess.exec(new String[]{"/bin/sh","-l"});
					System.out.println("执行终端命令");
                    System.out.println("之后要实现登录pod的命令");//不一定能实现
  					client.sendEvent("connect","terminal socket response");

                 } catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		//添加客户端断开连接事件
		server.addDisconnectListener(new DisconnectListener(){
			@Override
			public void onDisconnect(SocketIOClient client) {
				if( term != null){
					term.destroy();
				}
                System.out.println("链接断开");
            }
		});

		Thread.sleep(Integer.MAX_VALUE);

		server.stop();
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
	public void sendMessageToOneClient(String userName,String eventType,String message){
		try {
			if(userName != null && !"".equals(userName)){
				SocketIOClient client = (SocketIOClient)clientsMap.get(userName);
				if(client != null){
					client.sendEvent(eventType,message);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public static String convertStreamToString(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "UTF-8"), 8 * 1024);

            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            sb.delete(0, sb.length());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                return null;            }
        }

        return sb.toString();
    }
}
