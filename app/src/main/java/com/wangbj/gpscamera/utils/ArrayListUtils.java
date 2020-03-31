package com.wangbj.gpscamera.utils;

import com.wangbj.gpscamera.bean.Path;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author JackWang
 * @fileName ArrayListUtils
 * @date on 2020-01-01 下午 12:55
 * @email 544907049@qq.com
 **/
public class ArrayListUtils {

    /**
     * 用于将Path对象转化为HashMap，已弃用
     *
     * @param arrayList1
     * @return
     */
    public ArrayList<HashMap<String, String>> path2HashMap(ArrayList<Path> arrayList1) {
        ArrayList<HashMap<String, String>> arrayList2 = new ArrayList<>();
        for (int i = 0; i < arrayList1.size(); i++) {
            arrayList2.add(arrayList1.get(i).getHashMap());
        }
        return arrayList2;
    }



    public static ArrayList<Path> changeCloud(ArrayList<Path> arrayList, String id) {
        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).get("id").equals(id)) {
                arrayList.get(i).put("cloud", true);
                break;
            }
        }
        return arrayList;
    }

}
