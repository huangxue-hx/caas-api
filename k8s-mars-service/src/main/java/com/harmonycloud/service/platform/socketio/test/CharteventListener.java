package com.harmonycloud.service.platform.socketio.test;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;

/**
 * Created by andy on 17-2-23.
 */
public class CharteventListener implements DataListener<ChatObject> {

    private SocketIOServer server;

    public void setServer(SocketIOServer server) {
        this.server = server;
    }

    public void onData(SocketIOClient client, ChatObject data,
                       AckRequest ackSender) throws Exception {
        // chatevent为 事件的名称， data为发送的内容
        this.server.getBroadcastOperations().sendEvent("chatevent", data);
    }
}

