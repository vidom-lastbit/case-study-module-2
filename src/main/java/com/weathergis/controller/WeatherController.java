package com.weathergis.controller;

import com.weathergis.model.WeatherData;
import com.weathergis.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController //Điều khiển REST API
@RequestMapping("/api/weather") //Điều khiển API thời tiết
@CrossOrigin(origins = "*") //Cho phép truy cập từ mọi nguồn
public class WeatherController {

    @Autowired //Tiêm WeatherService để sử dụng
    private WeatherService weatherService;
    @GetMapping("/current") //Lấy dữ liệu thời tiết hiện tại
    public ResponseEntity<?> getCurrentWeather(
            @RequestParam Double lat, 
            @RequestParam Double lon,
            @RequestParam(required = false) String locationName) { 
        try {
            WeatherData data = weatherService.getWeather(lat, lon, locationName);
            return ResponseEntity.ok(data); 
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }
}