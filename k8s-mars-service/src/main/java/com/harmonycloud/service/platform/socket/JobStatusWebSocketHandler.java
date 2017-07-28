package com.harmonycloud.service.platform.socket;

import com.harmonycloud.service.platform.service.ci.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component("jobStatusWebSocketHandler")
public class JobStatusWebSocketHandler implements WebSocketHandler{

    @Autowired
    JobService jobService;

	private static final Logger logger = LoggerFactory.getLogger(JobStatusWebSocketHandler.class);

	public static final Map<String, WebSocketSession> userSocketSessionMap = new HashMap<>();



	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus arg1) throws Exception {
		logger.debug("连接已关闭");
        System.out.println("afterConnectionClosed");
		String username = session.getAttributes().get("userName").toString();
		userSocketSessionMap.remove(username);
	}


/**
     * webscoket建立好链接之后的处理函数
     *
     * @param session
     *            当前websocket的会话id，打开一个websocket通过都会生成唯一的一个会话，
     *            可以通过该id进行发送消息到浏览器客户端
     */

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("ConnectionEstablished");
        logger.info("成功建立socket连接");
		String username = session.getAttributes().get("userName").toString();
		userSocketSessionMap.put(username, session);
		if(userSocketSessionMap != null && userSocketSessionMap.containsKey(username)){
			userSocketSessionMap.remove(username);
			userSocketSessionMap.put(username, session);
		} else {
			userSocketSessionMap.put(username, session);
		}

        jobService.jobStatusWS(session, new Integer(session.getAttributes().get("id").toString()));


		//发送信息

        /*eventService.listenEvents();*/

	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        System.out.println("handleMessage" + message.toString());
        TextMessage returnMessage = new TextMessage(message.getPayload()+" received at server");// 获取提交过来的消息

		// template.convertAndSend("/topic/getLog", text); // 这里用于广播
		session.sendMessage(returnMessage);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable error) throws Exception {
		if(session.isOpen()){
			session.close();
		}
		logger.error("连接出现错误:"+error.toString());
		String username = session.getAttributes().get("username").toString();
		userSocketSessionMap.remove(username);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}


/**
     * 给某个用户发送消息
     *
     * @param userName
     * @param message
	 * @throws java.io.IOException
     */

    public void sendMessageToUser(String userName, TextMessage message) throws IOException {
    	WebSocketSession session = userSocketSessionMap.get(userName);
    	if (session != null && session.isOpen()) {  
            session.sendMessage(message);  
        }  

        /*for (WebSocketSession user : users) {
            if (user.getAttributes().get("user").equals(userName)) {
                try {
                    if (user.isOpen()) {
                        user.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }*/

    }

}

