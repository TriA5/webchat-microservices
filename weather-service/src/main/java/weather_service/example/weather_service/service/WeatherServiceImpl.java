package weather_service.example.weather_service.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import lombok.extern.slf4j.Slf4j;
import weather_service.example.weather_service.exception.WeatherServiceException;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

	@Value("${X-RapidAPI-Key}")
	private String rapidkey;

	@Value("${X-RapidAPI-Host}")
	private String rapidhost;
	
	@Value("${open-Key}")
	private String apikey;
	
	private static final String Summary_Url = "https://forecast9.p.rapidapi.com/rapidapi/forecast/";
    private static final String Hourly_Url = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String Current_Weather_Url = "https://api.openweathermap.org/data/2.5/weather";
    private static final String Geo_Url = "https://api.openweathermap.org/data/2.5/weather";
	
    RestTemplate restTemplate=new RestTemplate();

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
	
	@Override
public String GetForecastSummaryByLocationName(String city) {
    try {
        // Chuẩn hóa tên city (viết thường và bỏ khoảng trắng)
        city = city.trim().toLowerCase();

        // Forecast9 yêu cầu format: /country/city/summary
        // Nếu city không chứa '/', ta thêm mặc định 'Vietnam'
        if (!city.contains("/")) {
            city = "Vietnam/" + city;
        }

        String url = Summary_Url + city + "/summary/";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", rapidkey);
        headers.set("X-RapidAPI-Host", rapidhost);
        headers.set("Client-ID", "client-" + UUID.randomUUID().toString().substring(0, 8));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();

        return response;
    } catch (Exception e) {
        logger.error("Error retrieving weather summary for {}: {}", city, e.getMessage());
        throw new WeatherServiceException("Error retrieving weather summary for " + city + " details--" + e.getMessage());
    }
}


	@Override
	public String GetHourlyForecastByLocationName(String city) {
		try {
		String url = Hourly_Url+"?q="+city+"&appid="+apikey;
				//set random client id
		HttpHeaders headers = new HttpHeaders();
       headers.set("Client-ID","client-"+UUID.randomUUID().toString().substring(0, 8));

		HttpEntity<String> entity = new HttpEntity<>(headers);

		String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();

		return response;
		} catch (Exception e) {
            logger.error("Error retrieving hourly weather forecast for {}: {}", city, e.getMessage());
            throw new WeatherServiceException("Error retrieving hourly weather forecast for " + city+" details--"+e.getMessage());
        }
	}

	@Override
	public String GetCurrentWeatherByLocationName(String city) {
		try {
			// API OpenWeatherMap để lấy thời tiết hiện tại
			String url = Current_Weather_Url + "?q=" + city + "&appid=" + apikey + "&units=metric&lang=vi";
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Client-ID", "client-" + UUID.randomUUID().toString().substring(0, 8));
			
			HttpEntity<String> entity = new HttpEntity<>(headers);
			String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
			
			logger.info("Successfully retrieved current weather for: {}", city);
			return response;
		} catch (Exception e) {
			logger.error("Error retrieving current weather for {}: {}", city, e.getMessage());
			throw new WeatherServiceException("Error retrieving current weather for " + city + " details--" + e.getMessage());
		}
	}

	@Override
	public String GetWeatherByCoordinates(double lat, double lon) {
		try {
			// API OpenWeatherMap để lấy thời tiết theo tọa độ
			String url = Geo_Url + "?lat=" + lat + "&lon=" + lon + "&appid=" + apikey + "&units=metric&lang=vi";
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Client-ID", "client-" + UUID.randomUUID().toString().substring(0, 8));
			
			HttpEntity<String> entity = new HttpEntity<>(headers);
			String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
			
			logger.info("Successfully retrieved weather for coordinates: lat={}, lon={}", lat, lon);
			return response;
		} catch (Exception e) {
			logger.error("Error retrieving weather for coordinates lat={}, lon={}: {}", lat, lon, e.getMessage());
			throw new WeatherServiceException("Error retrieving weather for coordinates - details: " + e.getMessage());
		}
	}

	@Override
	public String GetFiveDayForecast(String city) {
		try {
			// API OpenWeatherMap để lấy dự báo 5 ngày
			String url = Hourly_Url + "?q=" + city + "&appid=" + apikey + "&units=metric&lang=vi&cnt=40";
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Client-ID", "client-" + UUID.randomUUID().toString().substring(0, 8));
			
			HttpEntity<String> entity = new HttpEntity<>(headers);
			String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
			
			logger.info("Successfully retrieved 5-day forecast for: {}", city);
			return response;
		} catch (Exception e) {
			logger.error("Error retrieving 5-day forecast for {}: {}", city, e.getMessage());
			throw new WeatherServiceException("Error retrieving 5-day forecast for " + city + " details--" + e.getMessage());
		}
	}

}

