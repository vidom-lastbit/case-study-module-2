# --- Giai đoạn 1: Build ứng dụng ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
# Build ra file .jar và bỏ qua bước test để tiết kiệm thời gian deploy
RUN mvn clean package -DskipTests

# --- Giai đoạn 2: Chạy ứng dụng ---
FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy file .jar từ giai đoạn build sang giai đoạn chạy
# Lưu ý: Tên file jar phải khớp với artifactId và version trong pom.xml
COPY --from=build /app/target/weather-gis-0.0.1-SNAPSHOT.jar app.jar

# Mở cổng 8080 (Cổng mặc định của Spring Boot)
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]