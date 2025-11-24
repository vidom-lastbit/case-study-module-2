package com.weathergis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity //Bản ghi dữ liệu thời tiết lịch sử
@Table(name = "weather_history") 
@Data
@AllArgsConstructor //Tạo constructor với tất cả các tham số
@NoArgsConstructor //Tạo constructor không tham số
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự động sinh ID
    private Long id;

    // --- CƠ BẢN ---
    private String name;
    private String conditionText;
    private String iconCode;
    private Double lat;
    private Double lng;
    
    // --- 1. NHIỆT ĐỘ & ĐỘ ẨM ---
    private Double temp;                // temperature
    private Double feelsLike;           // temperatureApparent
    private Double dewPoint;            // dewPoint
    private Integer humidity;           // humidity

    // --- 2. MƯA & TUYẾT ---
    private Double rain;                // precipitationIntensity (Cường độ chung)
    private Integer precipProbability;  // precipitationProbability
    private Integer precipType;         // precipitationType (Mã số)
    private Double rainIntensity;       // Chỉ mưa rào
    private Double snowIntensity;       // Tuyết
    private Double sleetIntensity;      // Mưa tuyết
    private Double freezingRainIntensity; // Mưa băng

    // --- 3. GIÓ & ÁP SUẤT ---
    private Double wind;                // windSpeed
    private Double windDirection;       // windDirection
    private Double windGust;            // windGust
    private Double pressureSurface;     // pressureSurfaceLevel
    private Double pressureSeaLevel;    // pressureSeaLevel

    // --- 4. BẦU TRỜI & TẦM NHÌN ---
    private Integer cloudCover;         // cloudCover
    private Double cloudBase;           // cloudBase
    private Double cloudCeiling;        // cloudCeiling
    private Double visibility;          // visibility

    // --- 5. BỨC XẠ & SỨC KHỎE ---
    private Integer uvIndex;            // uvIndex
    private Double solarGHI;            // solarGHI
    private Integer aqi;                // epaIndex (Nếu có)

    // --- THỜI GIAN ---
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @PrePersist // Gán thời gian hiện tại khi tạo bản ghi mới
    protected void onCreate() {
        this.recordedAt = LocalDateTime.now();
    }
    public String getLocationName() { return name; }
    public Double getTemperature() { return temp; }
    public Double getWindSpeed() { return wind; }
    public String getConditionText() { return conditionText; }
    public Double getRainVolume() { return rain; }
    public Double getLatitude() { return lat; }
    public Double getLongitude() { return lng; }
    public Integer getAqi() { return aqi; }
    public String getIconCode() { return iconCode; }
    public String getIcon() {
        return mapIconToEmoji(this.iconCode);
    }

    private String mapIconToEmoji(String code) {
        if (code == null) return "❓";
        if (code.startsWith("01")) return "☀️"; // Nắng
        if (code.startsWith("02")) return "🌤️"; // Nắng nhẹ
        if (code.startsWith("03")) return "⛅"; // Mây rải rác
        if (code.startsWith("04")) return "☁️"; // Âm u
        if (code.startsWith("09")) return "🌧️"; // Mưa rào
        if (code.startsWith("10")) return "🌦️"; // Mưa
        if (code.startsWith("11")) return "⚡"; // Dông
        if (code.startsWith("13")) return "❄️"; // Tuyết
        if (code.startsWith("50")) return "🌫️"; // Sương mù
        return "🌍";
    }
}