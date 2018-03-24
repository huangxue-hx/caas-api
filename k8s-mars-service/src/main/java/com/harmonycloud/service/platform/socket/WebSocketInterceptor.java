package com.harmonycloud.service.platform.socket;

import com.harmonycloud.common.Constant.CommonConstant;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public class WebSocketInterceptor implements HandshakeInterceptor{

	@Override
	public void afterHandshake(ServerHttpRequest arg0, ServerHttpResponse arg1, WebSocketHandler arg2, Exception arg3) {
	}


/**
	 * @desp 将HttpSession中对象放入WebSocketSession中
	 */

	@Override
	public boolean beforeHandshake(ServerHttpRequest request,ServerHttpResponse response,
			WebSocketHandler handler,Map<String, Object> map) throws Exception {
		if(request instanceof ServerHttpRequest){
			ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
			HttpSession session = servletRequest.getServletRequest().getSession();
			if(session!=null){
				//根据用户名区分区分socket连接以定向发送消息\
				map.put(CommonConstant.ROLEID, session.getAttribute(CommonConstant.ROLEID));
				map.put(CommonConstant.USERNAME, session.getAttribute(CommonConstant.USERNAME));
            }
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            if(httpServletRequest!=null){
				if(!StringUtils.isBlank(httpServletRequest.getParameter("projectId"))) {
					map.put("projectId", httpServletRequest.getParameter("projectId"));
				}
				if(!StringUtils.isBlank(httpServletRequest.getParameter("clusterId"))) {
					map.put("clusterId", httpServletRequest.getParameter("clusterId"));
				}
				if(!StringUtils.isBlank(httpServletRequest.getParameter("type"))) {
					map.put("type", httpServletRequest.getParameter("type"));
				}
				if(!StringUtils.isBlank(httpServletRequest.getParameter("buildNum"))) {
					map.put("buildNum", httpServletRequest.getParameter("buildNum"));
				}
                if(!StringUtils.isBlank(httpServletRequest.getParameter("buildNum"))) {
                    map.put("buildNum", httpServletRequest.getParameter("buildNum"));
                }
                if(!StringUtils.isBlank(httpServletRequest.getParameter("id"))) {
                    map.put("id", httpServletRequest.getParameter("id"));
                }
                if(!StringUtils.isBlank(httpServletRequest.getParameter("tenant"))) {
                    map.put("tenant", httpServletRequest.getParameter("tenant"));
                }
				if(!StringUtils.isBlank(httpServletRequest.getParameter("scriptType"))) {
					map.put("scriptType", httpServletRequest.getParameter("scriptType"));
				}
				if(!StringUtils.isBlank(httpServletRequest.getParameter("container"))) {
					map.put("container", httpServletRequest.getParameter("container"));
				}
				if(!StringUtils.isBlank(httpServletRequest.getParameter("pod"))) {
					map.put("pod", httpServletRequest.getParameter("pod"));
				}
				if(!StringUtils.isBlank(httpServletRequest.getParameter("namespace"))) {
					map.put("namespace", httpServletRequest.getParameter("namespace"));
				}
            }

		}
		return true;
	}

}

