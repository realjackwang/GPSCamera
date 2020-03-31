package com.wangbj.gpscamera;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

/**
 * @author JackWang
 * @fileName NoticeService
 * @date on 2020-01-16 下午 4:50
 * @email 544907049@qq.com
 **/
public class NoticeService extends Service {
    private NotificationChannel channel;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Android O上才显示通知栏

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //显示通知栏
    public void showNotify() {
        if (channel == null) {
            channel = createNotificationChannel();
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getPackageName());

        builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GPSCamera")
                .setContentText("正在记录轨迹")
                .setWhen(System.currentTimeMillis());
        Notification build = builder.build();
        //调用这个方法把服务设置成前台服务
        startForeground(110, build);
    }


    @TargetApi(Build.VERSION_CODES.O)
    private NotificationChannel createNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(getPackageName(), getPackageName(), NotificationManager.IMPORTANCE_HIGH);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        return notificationChannel;
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public NoticeService getService() {
            if (Build.VERSION.SDK_INT >= 26) {
                showNotify();
            }
            return NoticeService.this;
        }
    }


}
