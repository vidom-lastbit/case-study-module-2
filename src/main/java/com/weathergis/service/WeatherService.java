package com.weathergis.service;

import com.weathergis.model.WeatherData;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity; 
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service //Cho phép tiêm vào các lớp khác
public class WeatherService {

    @Value("${weather.api.key}") //Nhận khóa API của tommorrow.io từ file cấu hình
    private String apiKey;
    private final String API_URL = "https://api.tomorrow.io/v4/weather/realtime?location={lat},{lon}&apikey={key}";
    @SuppressWarnings("null") 
    public WeatherData getWeather(double lat, double lon, String locationName) {
        RestTemplate restTemplate = new RestTemplate();
        
        // Thay thế tham số vào URL
        String url = API_URL.replace("{lat}", String.valueOf(lat))
                            .replace("{lon}", String.valueOf(lon))
                            .replace("{key}", apiKey);
                            
        try {
            System.out.println("Calling Tomorrow.io API: " + url);
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            String responseBody = responseEntity.getBody();
            if (responseBody == null) {
                throw new RuntimeException("API trả về dữ liệu rỗng");
            }
            return parseResponse(responseBody, lat, lon, locationName);
            
        } catch (Exception e) {
            System.err.println("Lỗi gọi API Tomorrow.io: " + e.getMessage());
            e.printStackTrace();
            return new WeatherData(); 
        }
    }

    private WeatherData parseResponse(String jsonResponse, double lat, double lon, String locationName) {// Chuẩn hoá dữ liệu từ JSON trả về
        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONObject dataBlock = root.getJSONObject("data");
            JSONObject values = dataBlock.getJSONObject("values");

            WeatherData data = new WeatherData();
            if (locationName != null && !locationName.isEmpty()) {
                data.setName(locationName);
            } else {
                data.setName(String.format("Toạ độ: %.2f, %.2f", lat, lon));
            }
            data.setLat(lat);
            data.setLng(lon);

            // 2. NHÓM NHIỆT ĐỘ & ĐỘ ẨM
            data.setTemp(values.optDouble("temperature", 0.0));
            data.setFeelsLike(values.optDouble("temperatureApparent", 0.0));
            data.setDewPoint(values.optDouble("dewPoint", 0.0));
            data.setHumidity(values.optInt("humidity", 0));

            // 3. NHÓM MƯA & TUYẾT
            data.setRain(values.optDouble("precipitationIntensity", 0.0));
            data.setPrecipProbability(values.optInt("precipitationProbability", 0));
            data.setPrecipType(values.optInt("precipitationType", 0));
            
            // Các loại mưa chi tiết
            data.setRainIntensity(values.optDouble("rainIntensity", 0.0));
            data.setSnowIntensity(values.optDouble("snowIntensity", 0.0));
            data.setSleetIntensity(values.optDouble("sleetIntensity", 0.0));
            data.setFreezingRainIntensity(values.optDouble("freezingRainIntensity", 0.0));

            // 4. NHÓM GIÓ & ÁP SUẤT
            data.setWind(values.optDouble("windSpeed", 0.0));
            data.setWindDirection(values.optDouble("windDirection", 0.0));
            data.setWindGust(values.optDouble("windGust", 0.0));
            data.setPressureSurface(values.optDouble("pressureSurfaceLevel", 0.0));
            data.setPressureSeaLevel(values.optDouble("pressureSeaLevel", 0.0));

            // 5. NHÓM BẦU TRỜI & TẦM NHÌN
            data.setCloudCover(values.optInt("cloudCover", 0));
            data.setCloudBase(values.optDouble("cloudBase", 0.0));
            data.setCloudCeiling(values.optDouble("cloudCeiling", 0.0));
            data.setVisibility(values.optDouble("visibility", 10.0));

            // Xử lý Mã thời tiết (Code -> Text/Icon)
            int weatherCode = values.optInt("weatherCode", 1000);
            WeatherInfo info = decodeWeatherCode(weatherCode);
            data.setConditionText(info.description);
            data.setIconCode(info.iconCode);

            // 6. NHÓM BỨC XẠ & SỨC KHỎE
            data.setUvIndex(values.optInt("uvIndex", 0));
            data.setSolarGHI(values.optDouble("solarGHI", 0.0));
            data.setAqi(values.optInt("epaIndex", 0)); 

            return data;

        } catch (Exception e) {
            System.err.println("Lỗi Parse JSON Tomorrow.io: " + e.getMessage());
            e.printStackTrace();
            return new WeatherData();
        }
    }

    // --- HÀM PHỤ: Dịch mã Tomorrow.io sang Tiếng Việt & Mã Icon ---
    private WeatherInfo decodeWeatherCode(int code) {
        switch (code) {
            case 1000: return new WeatherInfo("Trời quang", "01d");
            case 1100: return new WeatherInfo("Nắng nhẹ", "02d");
            case 1101: return new WeatherInfo("Mây rải rác", "03d");
            case 1102: return new WeatherInfo("Trời nhiều mây", "04d");
            case 1001: return new WeatherInfo("Âm u", "04d");
            case 2000: 
            case 2100: return new WeatherInfo("Sương mù", "50d");
            case 4000: return new WeatherInfo("Mưa phùn", "09d");
            case 4001: return new WeatherInfo("Mưa rào", "10d");
            case 4200: return new WeatherInfo("Mưa nhẹ", "10d");
            case 4201: return new WeatherInfo("Mưa lớn", "09d");
            case 8000: return new WeatherInfo("Dông bão", "11d");
            case 5000: 
            case 5100: return new WeatherInfo("Tuyết rơi", "13d");
            case 6000: return new WeatherInfo("Mưa băng", "13d");
            case 6001: return new WeatherInfo("Mưa đá", "13d");
            default: return new WeatherInfo("Không xác định", "01d");
        }
    }

    // Class nội bộ dùng để trả về cặp thông tin (Mô tả + Icon)
    private static class WeatherInfo {
        String description;
        String iconCode;

        public WeatherInfo(String description, String iconCode) {
            this.description = description;
            this.iconCode = iconCode;
        }
    }
}