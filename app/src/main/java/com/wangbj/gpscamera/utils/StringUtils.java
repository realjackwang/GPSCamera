package com.wangbj.gpscamera.utils;

import java.math.BigDecimal;
import java.util.Random;

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

}
