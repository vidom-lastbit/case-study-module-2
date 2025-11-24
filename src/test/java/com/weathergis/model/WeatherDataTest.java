package com.weathergis.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherDataTest {

    @Test
    @DisplayName("Test chuyển đổi Icon Code sang Emoji")
    void testGetIcon() {
        WeatherData data = new WeatherData();

        data.setIconCode("01d"); // Mã nắng
        assertEquals("☀️", data.getIcon());

        data.setIconCode("10d"); // Mã mưa
        assertEquals("🌦️", data.getIcon());

        data.setIconCode("50d"); // Mã sương mù
        assertEquals("🌫️", data.getIcon());

        data.setIconCode(null); // Null
        assertEquals("❓", data.getIcon());

        data.setIconCode("999"); // Mã lạ
        assertEquals("🌍", data.getIcon());
    }
}