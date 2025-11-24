package com.weathergis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeatherScheduler {

    @Autowired //Tiêm SimpMessagingTemplate để gửi tin nhắn WebSocket
    private SimpMessagingTemplate template;
    @Scheduled(fixedRate = 30000) //Gửi tin nhắn mỗi 30 giây
    public void sendSystemStatus() {
        String message = "Hệ thống đang hoạt động ổn định lúc: " + java.time.LocalTime.now();
        template.convertAndSend("/topic/alerts", message); // Gửi tin nhắn đến tất cả các client đăng ký kênh /topic/alerts
    }
}