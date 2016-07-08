package com.temoa.sunnydoll;

/**
 * Created by Temoa
 * on 2016/5/16 16:24
 */
public class WeatherData {

    /**
     * errNum : 0
     * errMsg : success
     * retData : {"city":"湛江","pinyin":"zhanjiang","citycode":"101281001","date":"16-05-16","time":"11:00","postCode":"524000","longitude":110.384,"latitude":21.19,"altitude":"28","weather":"多云","temp":"26","l_tmp":"21","h_tmp":"26","WD":"无持续风向","WS":"微风(<10km/h)","sunrise":"05:59","sunset":"19:09"}
     */

    private int errNum;
    private String errMsg;
    /**
     * city : 湛江
     * pinyin : zhanjiang
     * citycode : 101281001
     * date : 16-05-16
     * time : 11:00
     * postCode : 524000
     * longitude : 110.384
     * latitude : 21.19
     * altitude : 28
     * weather : 多云
     * temp : 26
     * l_tmp : 21
     * h_tmp : 26
     * WD : 无持续风向
     * WS : 微风(<10km/h)
     * sunrise : 05:59
     * sunset : 19:09
     */

    private RetDataEntity retData;

    public int getErrNum() {
        return errNum;
    }

    public void setErrNum(int errNum) {
        this.errNum = errNum;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public RetDataEntity getRetData() {
        return retData;
    }

    public void setRetData(RetDataEntity retData) {
        this.retData = retData;
    }

    public static class RetDataEntity {
        private String weather;
        private String temp;
        private String l_tmp;
        private String h_tmp;
        private String WD;
        private String WS;

        public String getWeather() {
            return weather;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        public String getTemp() {
            return temp;
        }

        public void setTemp(String temp) {
            this.temp = temp;
        }

        public String getL_tmp() {
            return l_tmp;
        }

        public void setL_tmp(String l_tmp) {
            this.l_tmp = l_tmp;
        }

        public String getH_tmp() {
            return h_tmp;
        }

        public void setH_tmp(String h_tmp) {
            this.h_tmp = h_tmp;
        }

        public String getWD() {
            return WD;
        }

        public void setWD(String WD) {
            this.WD = WD;
        }

        public String getWS() {
            return WS;
        }

        public void setWS(String WS) {
            this.WS = WS;
        }
    }
}
