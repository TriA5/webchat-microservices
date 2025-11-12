package weather_service.example.weather_service.service;

public interface WeatherService {

	String GetForecastSummaryByLocationName(String city);
	String GetHourlyForecastByLocationName(String city);
	
	// API mới để lấy thời tiết hiện tại
	String GetCurrentWeatherByLocationName(String city);
	
	// API mới để lấy thời tiết theo tọa độ
	String GetWeatherByCoordinates(double lat, double lon);
	
	// API mới để lấy dự báo 5 ngày
	String GetFiveDayForecast(String city);
}

