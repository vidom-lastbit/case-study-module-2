package com.weathergis.controller;

import com.weathergis.model.WeatherData;
import com.weathergis.service.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    @DisplayName("GET /api/weather/current - Thành công")
    void testGetCurrentWeather() throws Exception {
        WeatherData mockData = new WeatherData();
        mockData.setName("Hanoi");
        mockData.setTemp(30.0);

        given(weatherService.getWeather(21.0, 105.0, "Hanoi")).willReturn(mockData);

        mockMvc.perform(get("/api/weather/current")
                .param("lat", "21.0")
                .param("lon", "105.0")
                .param("locationName", "Hanoi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hanoi"))
                .andExpect(jsonPath("$.temp").value(30.0));
    }
}