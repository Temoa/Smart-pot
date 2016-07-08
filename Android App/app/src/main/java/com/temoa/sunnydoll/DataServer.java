package com.temoa.sunnydoll;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Temoa
 * on 2016/5/16 15:45
 */
public interface DataServer {
    @GET("device/345349/sensor/{sensorId}/datapoints")
    Call<Data> getData(@Path("sensorId") long id);
}
