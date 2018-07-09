package com.harmonycloud.service.platform.socket;

import com.harmonycloud.dto.log.LogQueryDto;
import com.harmonycloud.service.platform.service.LogService;
import com.harmonycloud.service.platform.service.ci.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component("systemWebSocketHandler")
public class SystemWebSocketHandler implements WebSocketHandler{
	
    @Autowired
    JobService jobService;
    @Autowired
    LogService logService;
	
	private static final Logger logger = LoggerFactory.getLogger(SystemWebSocketHandler.class);




	public static final Map<String, WebSocketSession> userSocketSessionMap = new HashMap<>();

    
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus arg1) throws Exception {
		logger.debug("连接已关闭");
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
		String path = session.getUri().getPath();
		if("/rest/app/stderrlogs".equals(path)){
			String pod = (String) session.getAttributes().get("pod");
			String namespace = (String) session.getAttributes().get("namespace");
			String container = (String) session.getAttributes().get("container");
			String clusterId = (String) session.getAttributes().get("clusterId");
			LogQueryDto logQueryDto = new LogQueryDto();
			logQueryDto.setPod(pod);
			logQueryDto.setNamespace(namespace);
			logQueryDto.setContainer(container);
			logQueryDto.setClusterId(clusterId);
			logQueryDto.setLogSource(LogService.LOG_TYPE_STDOUT);
			logService.logRealTimeRefresh(session,logQueryDto);
		}
		if("/rest/app/filelogs".equals(path)){

			//kubectl exec webapi-6cf47949c8-kwddh -n kube-system -- tail -200f /opt/logs/webapi-info.2018-06-29.log
			String pod = (String) session.getAttributes().get("pod");
			String namespace = (String) session.getAttributes().get("namespace");
			String logDir = (String) session.getAttributes().get("logDir");
			String logFile = (String) session.getAttributes().get("logFile");
			String clusterId = (String) session.getAttributes().get("clusterId");
			LogQueryDto logQueryDto = new LogQueryDto();
			logQueryDto.setPod(pod);
			logQueryDto.setNamespace(namespace);
			logQueryDto.setLogDir(logDir);
			logQueryDto.setLogFile(logFile);
			logQueryDto.setClusterId(clusterId);
			logQueryDto.setLogSource(LogService.LOG_TYPE_LOGFILE);
			logService.logRealTimeRefresh(session,logQueryDto);
		}



		//发送信息

        /*eventService.listenEvents();*/

	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        TextMessage returnMessage = new TextMessage(message.getPayload()+" received at server");// 获取提交过来的消息
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
	 * @throws IOException 
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

