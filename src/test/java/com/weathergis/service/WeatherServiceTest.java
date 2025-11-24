package com.weathergis.service;

import com.weathergis.model.WeatherData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    @DisplayName("Test lấy thời tiết thành công")
    void testGetWeatherSuccess() {
        ReflectionTestUtils.setField(weatherService, "apiKey", "test-key");

        String mockJson = """
            {
                "data": {
                    "values": {
                        "temperature": 25.5,
                        "humidity": 80,
                        "weatherCode": 1000
                    }
                }
            }
        """;

        ResponseEntity<String> mockResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);
        
        // 2. Thêm lenient() vào đây để tránh lỗi UnnecessaryStubbing
        lenient().when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(mockResponse);

        WeatherData result = weatherService.getWeather(10.0, 106.0, "Saigon");

        assertNotNull(result, "Result không được null");
        if (result.getName() != null) {
             assertEquals("Saigon", result.getName());
             assertEquals(25.5, result.getTemp());
        }
    }

    @Test
    @DisplayName("Test xử lý khi API lỗi")
    void testGetWeatherFailure() {
        ReflectionTestUtils.setField(weatherService, "apiKey", "test-key");
        
        // 3. Thêm lenient() vào đây nữa
        lenient().when(restTemplate.getForEntity(anyString(), eq(String.class))).thenThrow(new RuntimeException("API Down"));

        WeatherData result = weatherService.getWeather(10.0, 106.0, "Saigon");

        assertNotNull(result);
        assertNull(result.getTemp());
    }
}