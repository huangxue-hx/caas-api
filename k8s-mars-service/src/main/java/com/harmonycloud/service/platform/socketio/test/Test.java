package com.harmonycloud.service.platform.socketio.test;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.stereotype.Service;

/**
 * Created by andy on 17-2-23.
 */
@Service("test")
public class Test {

    private static SocketIOServer server;

    public void startServer() throws InterruptedException{
        Configuration config = new Configuration();
        //服务器主机ip
        config.setHostname("10.100.100.150");
        //端口
        config.setPort(9090);
        config.setContext("/rest/notification");
        server = new SocketIOServer(config);
        CharteventListener listner = new CharteventListener();
        listner. setServer(server);
        server.addNamespace("/rest/notification");
        // chatevent为事件名称
        server.addEventListener("chatevent", ChatObject.class, listner);
        //启动服务
        server.start();
        Thread.sleep(Integer.MAX_VALUE) ;
        server.stop();
    }

    public static SocketIOServer getServer() {
        return server;
    }

}
