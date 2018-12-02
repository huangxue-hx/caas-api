package com.harmonycloud.service.platform.socketio;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andy on 17-2-22.
 */
@Service("socketIoService")
public class SocketIoService {
    private static SocketIOServer server;
    private static Map<String, SocketIOClient> clientsMap = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketIoService.class);

    public void startServer() throws InterruptedException{
        Configuration config = new Configuration();
        //服务器主机ip
        config.setHostname("10.100.100.150");
        //端口
        config.setPort(8082);
        config.setMaxFramePayloadLength(1024 * 1024);
        config.setMaxHttpContentLength(1024 * 1024);
//        config.setContext("/rest/notification");

        server = new SocketIOServer(config);

        server.start();
        SocketIONamespace socketIONamespace = server.addNamespace("/rest/notification");
        SocketIONamespace notification = server.addNamespace("/notification");

        for (int i = 0; i < 50; i++) {
            Thread.sleep(2000);
            notification.getBroadcastOperations().sendEvent("noti","客户端你好，我是服务端，有什么能帮助你的？"); //每次发送数字一
            socketIONamespace.getBroadcastOperations().sendEvent("noti","我是服务端，有什么能帮助你的？");

        }

        //监听广告推送事件，noti为事件名称，自定义
        server.addEventListener("noti", String.class, new DataListener<String>(){
            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws ClassNotFoundException {
                //客户端推送advert_info事件时，onData接受数据，这里是string类型的json数据，还可以为Byte[],object其他类型

                String sa = client.getRemoteAddress().toString();
                String clientIp = sa.substring(1,sa.indexOf(":"));//获取客户端连接的ip
                Map params = client.getHandshakeData().getUrlParams();//获取客户端url参数

                System.out.println(clientIp+"：客户端：************"+data);
            }
        });
        //监听通知事件
        server.addEventListener("notice_info", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                //同上
            }
        });

        /**
         * 监听其他事件
         */

        //添加客户端连接事件
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                // TODO Auto-generated method stub

                String sa = client.getRemoteAddress().toString();
                String clientIp = sa.substring(1,sa.indexOf(":"));//获取设备ip
                System.out.println(clientIp+"-------------------------"+"客户端已连接");
                Map params = client.getHandshakeData().getUrlParams();


                //获取客户端连接的uuid参数
                Object object = params.get("uuid");
                String uuid = "";
                if(object != null){
                    uuid = ((List<String>)object).get(0);
                    //将uuid和连接客户端对象进行绑定
                    clientsMap.put(uuid,client);
                }
                //给客户端发送消息
                client.sendEvent("noti",clientIp+"客户端你好，我是服务端，有什么能帮助你的？");
            }
        });
        //添加客户端断开连接事件
        server.addDisconnectListener(new DisconnectListener(){
            @Override
            public void onDisconnect(SocketIOClient client) {
                // TODO Auto-generated method stub
                String sa = client.getRemoteAddress().toString();
                String clientIp = sa.substring(1,sa.indexOf(":"));//获取设备ip
                System.out.println(clientIp+"-------------------------"+"客户端已断开连接");

                //给客户端发送消息
                client.sendEvent("noti",clientIp+"客户端你好，我是服务端，期待下次和你见面");
            }
        });


        Thread.sleep(Integer.MAX_VALUE);

        server.stop();
    }
    public void stopServer(){
        if(server != null){
            server.stop();
            server = null;
        }
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
            LOGGER.warn("sendMessageToOneClient失败", e);
        }
    }

    public static SocketIOServer getServer() {
        return server;
    }
}
