package com.weathergis.controller;

import com.weathergis.model.WeatherData;
import com.weathergis.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;
    @GetMapping("/current")
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