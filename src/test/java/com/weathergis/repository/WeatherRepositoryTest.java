package com.weathergis.repository;

import com.weathergis.model.WeatherData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class WeatherRepositoryTest {

    @Autowired
    private WeatherRepository weatherRepository;

    @Test
    @DisplayName("Tìm 10 bản ghi mới nhất")
    void testFindTop10() {
        // Tạo giả 12 bản ghi
        for (int i = 1; i <= 12; i++) {
            WeatherData data = new WeatherData();
            data.setName("Location " + i);
            data.setRecordedAt(LocalDateTime.now().plusMinutes(i));
            weatherRepository.save(data);
        }

        List<WeatherData> results = weatherRepository.findTop10ByOrderByRecordedAtDesc();

        // Kiểm tra chỉ lấy 10
        assertEquals(10, results.size());
        // Kiểm tra phần tử đầu tiên là cái mới nhất (Location 12)
        assertEquals("Location 12", results.get(0).getName());
    }
}