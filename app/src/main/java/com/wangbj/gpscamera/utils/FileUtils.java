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
    private String path = Environment.getExternalStorageDirectory().toString() + "/shidoe";

    public FileUtils() {
        File file = new File(path);
        /**
         *如果文件夹不存在就创建
         */
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 创建一个文件
     * @param FileName 文件名
     * @return
     */
    public File createFile(String FileName) {
        return new File(path, FileName);
    }


    public static void openFile(File file){

    }

}
