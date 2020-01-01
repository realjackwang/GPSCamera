package com.wangbj.gpscamera.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author JackWang
 * @fileName DataUtils
 * @date on 2020-01-01 下午 3:31
 * @email 544907049@qq.com
 **/
public class DataUtils {


    public static String  getCurrentTime(String format){
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String date = dateFormat.format(now);
        return date;
    }
}
