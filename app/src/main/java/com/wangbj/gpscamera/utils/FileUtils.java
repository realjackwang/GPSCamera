package com.wangbj.gpscamera.utils;

import android.os.Environment;

import java.io.File;

/**
 * @author JackWang
 * @fileName FileUtils
 * @date on 2019-12-02 下午 5:03
 * @email 544907049@qq.com
 **/
public class FileUtils {
//    private String path = Environment.getExternalStorageDirectory().toString() + "/shidoe";

    public static void createFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    public static boolean checkFile(String path) {
        File file = new File(path);
        return file.exists();
    }


}
