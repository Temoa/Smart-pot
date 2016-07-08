package com.temoa.sunnydoll;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String APIKEY = "your Yeelink Apikey";
    private AMapLocationClient mLocationClient = null;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private DataServer dataServer;
    private WeatherServer weatherServer;
    private Call<Data> call1, call2, call4;
    private Call<WeatherData> call3;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private String cityName;
    private TextView tvCityName, tvTemp, tvMoisture;
    private int temp = 0;
    private int moisture = 0;
    private TextView tvWeather, tvMinTemp, tvMaxTemp, tvWD, tvWS, tvWaterbox;
    private ImageView ivWeatherIcon, ivPlant;
    private String weather, l_temp, h_temp, wd, ws;
    private int weaIconId;

    private long exitTime = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        cityName = sharedPreferences.getString("cityName", "湛江");
        tvCityName.setText(cityName);
        initAMap();
        mLocationClient.startLocation();
        initRetrofit();
        initWeatherRetrofit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPlantData();
        getWeather();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationClient.stopLocation();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.onDestroy();
        call1.cancel();
        call2.cancel();
        call3.cancel();
        call4.cancel();
        super.onDestroy();
    }

    private void initView() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.layout_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary);

        tvCityName = (TextView) findViewById(R.id.tv_cityName);
        tvMoisture = (TextView) findViewById(R.id.tv_moisture_num);
        tvTemp = (TextView) findViewById(R.id.tv_temp);
        tvWeather = (TextView) findViewById(R.id.tv_weather);
        tvMinTemp = (TextView) findViewById(R.id.tv_minTemp);
        tvMaxTemp = (TextView) findViewById(R.id.tv_maxTemp);
        tvWD = (TextView) findViewById(R.id.tv_wd);
        tvWS = (TextView) findViewById(R.id.tv_ws);
        tvWaterbox = (TextView) findViewById(R.id.tv_waterBox_num);
        ivWeatherIcon = (ImageView) findViewById(R.id.iv_weatherIcon);
        ivPlant = (ImageView) findViewById(R.id.iv_plant);
        ivPlant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PumpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initAMap() {
        final AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//定位模式为精确模式
        mLocationOption.setNeedAddress(true);
        mLocationOption.setOnceLocation(true);
        mLocationOption.setWifiActiveScan(true);
        mLocationOption.setMockEnable(false);//模拟位置，默认为关闭
        mLocationOption.setInterval(2000);//定位时间间隔，默认2000ms
        mLocationClient = new AMapLocationClient(this);
        mLocationClient.setLocationOption(mLocationOption);
        AMapLocationListener mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation.getErrorCode() == 0) {
                    String cityName = aMapLocation.getCity();
                    editor = sharedPreferences.edit();
                    if (cityName != null && cityName.equals("")) {
                        editor.putString("cityName", cityName.indexOf(2, 0) + "");
                        editor.apply();
                    } else {
                        editor.putString("cityName", "湛江");
                        editor.apply();
                    }
                } else {
                    Log.e("AmapError",
                            "location Error, ErrCode:"
                                    + aMapLocation.getErrorCode()
                                    + ", errInfo:"
                                    + aMapLocation.getErrorInfo());
                }
            }
        };
        mLocationClient.setLocationListener(mLocationListener);
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.yeelink.net/v1.0/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        dataServer = retrofit.create(DataServer.class);
    }

    private void initWeatherRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://apis.baidu.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherServer = retrofit.create(WeatherServer.class);
    }

    private void getPlantData() {
        call1 = dataServer.getData(384391);
        call1.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, retrofit2.Response<Data> response) {
                Data data = response.body();
                temp = data.getValue();
                if (temp != 0) {
                    tvTemp.setText(temp + "");
                } else {
                    tvTemp.setText("27");
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.e("initRetrofit", t.getMessage());
            }
        });
        call2 = dataServer.getData(384782);
        call2.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                Data data = response.body();
                moisture = data.getValue();
                if (moisture != 0) {
                    if (moisture == 100)
                        moisture = 98;
                    tvMoisture.setText(moisture + "");
                } else {
                    tvMoisture.setText("78");
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.e("initRetrofit", t.getMessage());
            }
        });
        call4 = dataServer.getData(389655);
        call4.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                Data data = response.body();
                int state = data.getValue();
                if (state == 0) {
                    tvWaterbox.setText("水量不足");
                } else {
                    tvWaterbox.setText("水量充足");
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.e("initRetrofit", t.getMessage());
            }
        });
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void getWeather() {
        call3 = weatherServer.getWeatherData(APIKEY, cityName);
        call3.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                WeatherData data = response.body();
                weather = data.getRetData().getWeather();
                l_temp = data.getRetData().getL_tmp();
                h_temp = data.getRetData().getH_tmp();
                wd = data.getRetData().getWD();
                ws = data.getRetData().getWS();

                tvWeather.setText(weather);
                tvMinTemp.setText(l_temp);
                tvMaxTemp.setText(h_temp);
                tvWD.setText(wd);
                tvWS.setText(ws);

                ivWeatherIcon.setImageResource(weatherIconSelect(weather));
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                Log.e("initRetrofit", t.getMessage());
            }
        });
    }

    private int weatherIconSelect(String weather) {
        if (weather.equals("晴")) {
            weaIconId = R.drawable.ic_weather_sunny;
        } else if (weather.equals("多云") | weather.equals("阴")) {
            weaIconId = R.drawable.ic_weather_duoyun;
        } else if (weather.equals("阵雨")) {
            weaIconId = R.drawable.ic_weather_shower;
        } else if (weather.equals("雷阵雨")) {
            weaIconId = R.drawable.ic_weather_thundershower;
        } else if (weather.equals("雷阵雨伴有冰雹")) {
            weaIconId = R.drawable.ic_weather_hail;
        } else if (weather.equals("雨夹雪")) {
            weaIconId = R.drawable.ic_weather_rainandsnow;
        } else if (weather.equals("小雨")) {
            weaIconId = R.drawable.ic_weather_smallrain;
        } else if (weather.equals("中雨") | weather.equals("小到中雨")) {
            weaIconId = R.drawable.ic_weather_minrain;
        } else if (weather.equals("大雨") | weather.equals("中到大雨") | weather.equals("暴雨")
                | weather.equals("大暴雨") | weather.equals("大到暴雨") | weather.equals("暴雨到大暴雨")
                | weather.equals("特大暴雨") | weather.equals("大暴雨到特大暴雨")) {
            weaIconId = R.drawable.ic_weather_bigrain;
        } else if (weather.equals("阵雪") | weather.equals("暴雪") | weather.equals("大到暴雪")) {
            weaIconId = R.drawable.ic_weather_bailzzard;
        } else if (weather.equals("雾") | weather.equals("霾") | weather.equals("浮尘") | weather.equals("扬沙")) {
            weaIconId = R.drawable.ic_weather_haze;
        } else if (weather.equals("沙尘暴") | weather.equals("强沙尘暴")) {
            weaIconId = R.drawable.ic_weather_sandstorm;
        } else if (weather.equals("小雪")) {
            weaIconId = R.drawable.ic_weather_smallsnow;
        } else if (weather.equals("中雪") | weather.equals("小到中雪")) {
            weaIconId = R.drawable.ic_weather_minsnow;
        } else if (weather.equals("大雪") | weather.equals("中到大雪")) {
            weaIconId = R.drawable.ic_weather_bigsnow;
        }
        return weaIconId;
    }

    @Override
    public void onRefresh() {
        getPlantData();
        getWeather();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一下退出应用",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
