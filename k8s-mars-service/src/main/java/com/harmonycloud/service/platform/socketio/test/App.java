package com.harmonycloud.service.platform.socketio.test;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

/**
 * Created by andy on 17-2-23.
 */
public class App {
    public static void main(String[] args) throws InterruptedException
    {
        Configuration config = new Configuration();
        config.setHostname("10.100.100.150");
        config.setPort(9092);
        config.setContext("/rest/notification");

        SocketIOServer server = new SocketIOServer(config);
        CharteventListener listner = new CharteventListener();
        listner. setServer(server);
        // chatevent为事件名称
        server.addNamespace("/rest/notification");
        server.addEventListener("chatevent", ChatObject.class, listner);
        //启动服务
        server.start();
        Thread.sleep(Integer.MAX_VALUE) ;
        server.stop();
    }
}