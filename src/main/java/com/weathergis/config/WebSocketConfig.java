package com.weathergis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration //Cấu hình WebSocket
@EnableWebSocketMessageBroker //Kích hoạt WebSocket với STOMP
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override 
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) { //Đăng ký điểm cuối WebSocket
        registry.addEndpoint("/ws-weather").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) { //Cấu hình bộ định tuyến tin nhắn
        registry.enableSimpleBroker("/topic"); // Cho cách lệnh bắt đầu bằng /topic được gửi đến bộ định tuyến tin nhắn
        registry.setApplicationDestinationPrefixes("/app"); // Cho các cách lệnh bắt đầu bằng /app được gửi đến các phương thức xử lý
    }
}