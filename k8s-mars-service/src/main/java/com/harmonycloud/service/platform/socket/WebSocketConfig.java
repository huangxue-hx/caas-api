package com.harmonycloud.service.platform.socket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebMvc
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(cicdWebSocketHandler(), "/cicd/job/log").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*");

        registry.addHandler(cicdWebSocketHandler(), "/cicd/job/log").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*").withSockJS();

        registry.addHandler(cicdWebSocketHandler(), "/cicd/job/status").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*");

        registry.addHandler(cicdWebSocketHandler(), "/cicd/job/status").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*").withSockJS();

        registry.addHandler(cicdWebSocketHandler(), "/cicd/stage/log").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*");

        registry.addHandler(cicdWebSocketHandler(), "/cicd/stage/log").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*").withSockJS();

        registry.addHandler(cicdWebSocketHandler(), "/cicd/job/jobList").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*");

        registry.addHandler(cicdWebSocketHandler(), "/cicd/job/jobList").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*").withSockJS();

        registry.addHandler(terminalSocketHandler(), "/terminal").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*");

        registry.addHandler(terminalSocketHandler(), "/terminal").addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*").withSockJS();

	}

    @Bean
    public CicdWebSocketHandler cicdWebSocketHandler(){
        return new CicdWebSocketHandler();
    }

	@Bean
	public HandshakeInterceptor webSocketInterceptor(){
		return new WebSocketInterceptor();
	}

    @Bean
    public WebSocketHandler terminalSocketHandler(){
        WebSocketHandler webSocketHandler = new PerConnectionWebSocketHandler(TerminalSocketHandler.class);
        return webSocketHandler;
    }
}
