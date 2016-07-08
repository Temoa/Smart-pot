package com.temoa.sunnydoll;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Created by Temoa
 * on 2016/5/16 16:37
 */
public interface WeatherServer {
    @GET("apistore/weatherservice/cityname")
    Call<WeatherData> getWeatherData(@Header("apikey") String apikey, @Query("cityname") String cityName);
}
