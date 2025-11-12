package weather_service.example.weather_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import weather_service.example.weather_service.service.WeatherService;


@RestController
// @CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/weathers")
@RequiredArgsConstructor
public class WeatherController {
    
    @Autowired
	private WeatherService implService;

    @GetMapping("/hello")
    public String getWeatherInfo() {
        return "Hello From Weather Service";
    }

	@GetMapping("/getForcast-Summary")
	ResponseEntity<String> GetForecastSummaryByLocationName(@RequestParam String city) {

		return new ResponseEntity<String>(implService.GetForecastSummaryByLocationName(city), HttpStatus.ACCEPTED);

	}

	@GetMapping("/getForcast-hourly")
	ResponseEntity<String> GetHourlyForecastByLocationName(@RequestParam String city) {

		return new ResponseEntity<String>(implService.GetHourlyForecastByLocationName(city), HttpStatus.ACCEPTED);

	}
	
	// API mới: Lấy thời tiết hiện tại
	@GetMapping("/current")
	ResponseEntity<String> GetCurrentWeather(@RequestParam String city) {
		return new ResponseEntity<String>(implService.GetCurrentWeatherByLocationName(city), HttpStatus.OK);
	}
	
	// API mới: Lấy thời tiết theo tọa độ
	@GetMapping("/by-coordinates")
	ResponseEntity<String> GetWeatherByCoordinates(@RequestParam double lat, @RequestParam double lon) {
		return new ResponseEntity<String>(implService.GetWeatherByCoordinates(lat, lon), HttpStatus.OK);
	}
	
	// API mới: Lấy dự báo 5 ngày
	@GetMapping("/forecast-5day")
	ResponseEntity<String> GetFiveDayForecast(@RequestParam String city) {
		return new ResponseEntity<String>(implService.GetFiveDayForecast(city), HttpStatus.OK);
	}
}
