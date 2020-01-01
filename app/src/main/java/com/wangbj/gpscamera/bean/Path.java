package com.wangbj.gpscamera.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author JackWang
 * @fileName Path
 * @date on 2019-12-31 下午 11:14
 * @email 544907049@qq.com
 **/
public class Path extends HashMap<String,Object> {

    private String id;
    private String title;
    private String info;
    private String starttime;
    private String endtime;

    private ArrayList<String[]> arrayList;


    /**
     * @param id 轨迹id
     * @param title 轨迹标题
     * @param info 轨迹地址信息
     * @param starttime 轨迹开始时间
     * @param endtime 轨迹结束时间
     * @param arrayList 轨迹
     */
    public Path(long id, String title, String info, String starttime, String endtime, ArrayList arrayList){
        super();
        super.put("id",id);
        super.put("title",title);
        super.put("info",info);
        super.put("starttime",starttime);
        super.put("endtime",endtime);
        super.put("path",arrayList);
    }

    public Path(){
        super();
    }


    public HashMap getHashMap(){
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("noticeTitle",this.title  );
        hashMap.put("noticeText", this.info);
        hashMap.put("noticeDate", this.starttime);
        hashMap.put("noticeTime", this.endtime);
        hashMap.put("Id", this.id);
        return hashMap;
    }


}
