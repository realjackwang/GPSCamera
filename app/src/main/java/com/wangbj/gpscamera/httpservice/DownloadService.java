package com.wangbj.gpscamera.httpservice;

import android.os.Environment;
import android.os.Message;
import android.util.Log;

import com.wangbj.gpscamera.ui.home.HomeFragment;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author JackWang
 * @fileName DownloadService
 * @date on 2019-12-02 下午 5:02
 * @email 544907049@qq.com
 **/
public class DownloadService {


    public static void download(String url, final OnDownloadListener listener) {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onDownloadFailed(e);
                Log.i("myTag", "下载失败:服务器无法连接");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    writeFile(response, listener);
                }
            }
        });

    }

    private static void writeFile(Response response, OnDownloadListener listener) {

        InputStream is = null;
        FileOutputStream fos = null;
        is = response.body().byteStream();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(path,"test.zip");
        try {
            fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int len = 0;

            //获取下载的文件的大小
            long fileSize = response.body().contentLength();
            long sum = 0;
            int porSize = 0;

            while ((len = is.read(bytes)) != -1) {
                fos.write(bytes);
                sum += len;

                porSize = (int) ((sum * 1.0f / fileSize) * 100);
                listener.onDownloading(porSize);

            }
            fos.flush();
            listener.onDownloadSuccess(file);
            Log.i("DownloadService", "下载成功");

        } catch (Exception e) {
            e.printStackTrace();
            listener.onDownloadFailed(e);
            Log.i("DownloadService", "下载失败:服务器连接异常");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




    }

    public interface OnDownloadListener {

        void onDownloadSuccess(File file);

        void onDownloading(int progress);

        void onDownloadFailed(Exception e);
    }

}
