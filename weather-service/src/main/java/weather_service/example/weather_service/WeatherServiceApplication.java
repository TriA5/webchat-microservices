package weather_service.example.weather_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient   // ← Enable Eureka Client
public class WeatherServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(WeatherServiceApplication.class, args);
	}
}
