package com.wangbj.gpscamera.utils;

import java.math.BigDecimal;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author JackWang
 * @fileName StringUtils
 * @date on 2020-01-01 下午 2:12
 * @email 544907049@qq.com
 **/
public class StringUtils {

    public static Double randomLonLat(double MinLat, double MaxLat, double MinLon, double MaxLon, String type) {

        Double lon = Math.random() * (MaxLon - MinLon) + MinLon;

        Double lat = Math.random() * (MaxLat - MinLat) + MinLat;

        if (type.equals("Lon")) {
            return lon;
        } else {
            return lat;
        }
    }


     public static boolean isPhone(String phone){
         // TODO: 2020-03-26 动态调节正则表达式
         String PHONE_PATTERN="^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(16[6])|(17([0,1,6,7,8]))|(18[0-2,5-9])|(19[0-9]))\\d{8}$";
         return phone.matches(PHONE_PATTERN);
     }

}
