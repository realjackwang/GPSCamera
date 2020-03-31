package com.wangbj.gpscamera.httpservice;

import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.wangbj.gpscamera.bean.Path;
import com.wangbj.gpscamera.utils.ACache;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * @author JackWang
 * @fileName JsonService
 * @date on 2020-03-25 下午 3:42
 * @email 544907049@qq.com
 **/
public class JsonService {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void Send(ACache aCache, String _url, JSONArray jsonArray, final JsonService.OnHttpListener onHttpListener) {

        String url = aCache.getAsString("CACHE_SERVER");
        String sessionId = aCache.getAsString("sessionid");

        if (sessionId == null) {
            sessionId = " ";
        }

        RequestBody formBody = RequestBody.create(JSON, jsonArray.toJSONString());
        try {
            Request request = new Request.Builder().url(url + _url).addHeader("cookie", sessionId).post(formBody).build();
            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: " + e.getMessage());
                    onHttpListener.onHttpFailed(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, response.protocol() + " " + response.code() + " " + response.message());
//                if (response.body() != null) {
//                    Log.d(TAG, "onResponse: " + response.body().string());
//                }
                    onHttpListener.onHttpSuccess(response);
                }
            });
        } catch (IllegalArgumentException e) {
            onHttpListener.onHttpFailed(e);
        }
    }


    public interface OnHttpListener {

        void onHttpSuccess(Response response) throws IOException;

        void onHttpFailed(Exception e);
    }
}
