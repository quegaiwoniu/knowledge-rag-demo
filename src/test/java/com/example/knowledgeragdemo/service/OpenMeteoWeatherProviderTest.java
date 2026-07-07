package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppWeatherProperties;
import com.example.knowledgeragdemo.dto.WeatherToolResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenMeteoWeatherProviderTest {

    @Test
    void getWeatherMapsOpenMeteoResponseToToolResult() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        AppWeatherProperties properties = new AppWeatherProperties(
                false,
                "https://geocoding-api.open-meteo.com/v1/search",
                "https://api.open-meteo.com/v1/forecast"
        );

        server.expect(requestTo("https://geocoding-api.open-meteo.com/v1/search?name=%E4%B8%8A%E6%B5%B7&count=1&language=zh&format=json"))
                .andRespond(withSuccess("""
                        {
                          "results": [
                            {
                              "name": "上海",
                              "latitude": 31.2222,
                              "longitude": 121.4581
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://api.open-meteo.com/v1/forecast?latitude=31.2222&longitude=121.4581&current=temperature_2m,relative_humidity_2m,weather_code,wind_direction_10m"))
                .andRespond(withSuccess("""
                        {
                          "current": {
                            "temperature_2m": 31,
                            "relative_humidity_2m": 70,
                            "weather_code": 3,
                            "wind_direction_10m": 90
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider(restClient, properties);

        WeatherToolResult result = provider.getWeather("上海");

        assertEquals("open-meteo", provider.source());
        assertEquals("上海", result.location());
        assertEquals("多云", result.condition());
        assertEquals(31, result.temperatureCelsius());
        assertEquals(70, result.humidityPercent());
        assertEquals("东风", result.windDirection());
    }

    @Test
    void getWeatherThrowsReadableMessageWhenLocationIsMissing() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        AppWeatherProperties properties = new AppWeatherProperties(
                false,
                "https://geocoding-api.open-meteo.com/v1/search",
                "https://api.open-meteo.com/v1/forecast"
        );

        server.expect(requestTo("https://geocoding-api.open-meteo.com/v1/search?name=%E7%81%AB%E6%98%9F&count=1&language=zh&format=json"))
                .andRespond(withSuccess("""
                        {
                          "results": []
                        }
                        """, MediaType.APPLICATION_JSON));

        OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider(restClient, properties);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> provider.getWeather("火星"));

        assertEquals("weather location not found: 火星", exception.getMessage());
    }
}
