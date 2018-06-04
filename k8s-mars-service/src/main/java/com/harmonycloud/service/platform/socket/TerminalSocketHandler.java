package com.harmonycloud.service.platform.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmonycloud.service.platform.socket.term.TerminalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TerminalSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(TerminalSocketHandler.class);

    private final TerminalService terminalService;

    @Autowired
    public TerminalSocketHandler(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        terminalService.setWebSocketSession(session);
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, String> messageMap = getMessageMap(message);
        if (messageMap.containsKey("type")) {
            String type = messageMap.get("type");
            switch (type) {
                case "TERMINAL_INIT":
                    terminalService.onTerminalInit();
                    break;
                case "TERMINAL_READY":
                    String scriptType = session.getAttributes().get("scriptType").toString();
                    String container = session.getAttributes().get("container").toString();
                    String pod = session.getAttributes().get("pod").toString();
                    String namespace = session.getAttributes().get("namespace").toString();
                    String clusterId = null;
                    if(session.getAttributes().get("clusterId") != null){
                        clusterId = session.getAttributes().get("clusterId").toString();
                    }
                    logger.info(String.format("进入控制台，容器名称:%s,pod名称:%s,namespace名称:%s,shell类型:%s",container,pod,namespace,scriptType));
                    terminalService.onTerminalReady(container,pod,namespace,clusterId,scriptType);
                    break;
                case "TERMINAL_COMMAND":
                    terminalService.onCommand(messageMap.get("command"));
                    break;
                case "TERMINAL_RESIZE":
                    terminalService.onTerminalResize(messageMap.get("columns"), messageMap.get("rows"));
                    break;
                default:
                    throw new RuntimeException("Unrecodnized action");
            }
        }
    }

    private Map<String, String> getMessageMap(TextMessage message) {
        try {
            Map<String, String> map = new ObjectMapper().readValue(message.getPayload(), new TypeReference<Map<String, String>>() {
            });

            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }

    @Override
    public boolean supportsPartialMessages() {
        return super.supportsPartialMessages();
    }
}
