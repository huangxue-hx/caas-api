package com.harmonycloud.service.platform.socket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebMvc
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// 注册处理拦截器,拦截url为socketServer的请求
		//registry.addHandler(systemWebSocketHandler(), "/notification").addInterceptors(webSocketInterceptor())
		//		.setAllowedOrigins("*");

		// 注册SockJs的处理拦截器,拦截url为/sockjs/socketServer的请求
		//registry.addHandler(systemWebSocketHandler(), "/notification").addInterceptors(webSocketInterceptor())
		//		.setAllowedOrigins("*").withSockJS();

        registry.addHandler(systemWebSocketHandler(), "/ci/job/buildLog").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*");

        registry.addHandler(systemWebSocketHandler(), "/ci/job/buildLog").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*").withSockJS();

        registry.addHandler(jobStatusWebSocketHandler(), "/ci/job").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*");

        registry.addHandler(jobStatusWebSocketHandler(), "/ci/job").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*").withSockJS();

	}

	@Bean
	public WebSocketHandler systemWebSocketHandler(){
		return new SystemWebSocketHandler();
	}

    @Bean
    public JobStatusWebSocketHandler jobStatusWebSocketHandler(){
        return new JobStatusWebSocketHandler();
    }

	@Bean
	public HandshakeInterceptor webSocketInterceptor(){
		return new WebSocketInterceptor();
	}
}
