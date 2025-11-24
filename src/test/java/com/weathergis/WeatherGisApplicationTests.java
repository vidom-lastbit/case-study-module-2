package com.weathergis;

import com.weathergis.service.WeatherScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;

@SpringBootTest
@SuppressWarnings("null")
class WeatherGisApplicationTests {

    @Autowired
    private WeatherScheduler weatherScheduler;

    @MockBean
    private SimpMessagingTemplate template;

    @Test
    @DisplayName("Kiểm tra Application Context load thành công")
    void contextLoads() {
    }

    @Test
    @DisplayName("Kiểm tra Scheduler gửi tin nhắn WebSocket")
    void testScheduler() {
        weatherScheduler.sendSystemStatus();
        verify(template, atLeastOnce()).convertAndSend(eq("/topic/alerts"), anyString());
    }
}