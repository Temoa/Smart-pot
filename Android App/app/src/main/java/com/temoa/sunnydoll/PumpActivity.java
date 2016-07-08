package com.temoa.sunnydoll;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PumpActivity extends AppCompatActivity {
    private final static String ON = "1";
    private final static String OFF = "0";

    private ImageView ivWater;
    private boolean isWater;

    private DataServer dataServer;
    private Call<Data> call, call2, call3;

    private TextView tvMoisture, tvWaterbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pump);
        initView();
        initRetrofit();
        getData();
        getSwitchData();

        ivWater = (ImageView) findViewById(R.id.iv_water);
        ivWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWater) {
                    AnimationSet set = new AnimationSet(false);
                    Animation rotate = AnimationUtils.loadAnimation(PumpActivity.this, R.anim.tip);
                    Animation alpha = AnimationUtils.loadAnimation(PumpActivity.this, R.anim.alpha);
                    LinearInterpolator lin = new LinearInterpolator();
                    set.setInterpolator(lin);
                    set.addAnimation(rotate);
                    set.addAnimation(alpha);
                    set.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            ivWater.setClickable(false);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            ivWater.setImageResource(R.drawable.ic_water_on);
                            ivWater.clearAnimation();
                            ivWater.setClickable(true);
                            postData(ON);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    ivWater.startAnimation(set);
                    isWater = false;
                } else {
                    AnimationSet set = new AnimationSet(false);
                    Animation rotate = AnimationUtils.loadAnimation(PumpActivity.this, R.anim.tip_n);
                    Animation alpha = AnimationUtils.loadAnimation(PumpActivity.this, R.anim.alpha);
                    LinearInterpolator lin = new LinearInterpolator();
                    set.setInterpolator(lin);
                    set.addAnimation(rotate);
                    set.addAnimation(alpha);
                    set.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            ivWater.setClickable(false);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            ivWater.setImageResource(R.drawable.ic_water_off);
                            ivWater.clearAnimation();
                            ivWater.setClickable(true);
                            postData(OFF);
                            getData();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    ivWater.startAnimation(set);
                    isWater = true;
                }
            }
        });
        if (isWater) {
            ivWater.setImageResource(R.drawable.ic_water_on);
        } else {
            ivWater.setImageResource(R.drawable.ic_water_off);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        call.cancel();
        postData(OFF);
    }

    private void initView() {
        tvMoisture = (TextView) findViewById(R.id.tv_moisture_num);
        tvWaterbox = (TextView) findViewById(R.id.tv_waterBox_num);
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.yeelink.net/v1.0/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        dataServer = retrofit.create(DataServer.class);
    }

    private void getSwitchData() {
        call = dataServer.getData(388863);
        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, retrofit2.Response<Data> response) {
                Data data = response.body();
                if (data.getValue() == 1) {
                    isWater = false;
                } else {
                    isWater = true;
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.e("initRetrofit", t.getMessage());
            }
        });
    }

    private void postData(final String value) {
        new Thread(new Runnable() {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/octet-stream");
            RequestBody body = RequestBody.create(mediaType, "{\"value\":" + value + "}");
            Request request = new Request.Builder()
                    .url("http://api.yeelink.net/v1.0/device/345349/sensor/388863/datapoints")
                    .post(body)
                    .addHeader("u-apikey", "Your Yeelink Apikey")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "5909f9fc-6ac6-49da-750c-a7792c319121")
                    .build();

            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void getData() {
        call2 = dataServer.getData(384782);//土壤湿度
        call2.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, retrofit2.Response<Data> response) {
                Data data = response.body();
                int hum = data.getValue();
                if (hum != 0) {
                    tvMoisture.setText(hum + "");
                } else {
                    tvMoisture.setText("64");
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.i("getData -> pumpActivity", t.getMessage());
            }
        });

        call3 = dataServer.getData(389655);//水箱
        call3.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, retrofit2.Response<Data> response) {
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
                Log.i("getData -> pumpActivity", t.getMessage());
            }
        });
    }
}
