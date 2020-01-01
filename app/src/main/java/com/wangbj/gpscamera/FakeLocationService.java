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
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.wangbj.gpscamera.utils.ACache;
import com.wangbj.gpscamera.utils.StringUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static android.content.ContentValues.TAG;

/**
 * @author JackWang
 * @fileName FakeLocationService
 * @date on 2020-01-01 下午 2:00
 * @email 544907049@qq.com
 * 用于室内进行GPS轨迹测试，伪造变化的GPS的信息。
 **/
public class FakeLocationService extends Service {

    private NotificationChannel channel;
    private LocalBinder localBinder = new LocalBinder();
    Handler mHandler;
    Runnable r;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "--->onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "--->onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }


    public Location startGps(){

        mHandler = new Handler();
         r= new Runnable() {

            @Override
            public void run() {
                Location location = new Location(LocationManager.GPS_PROVIDER);

                location.setLatitude(StringUtils.randomLonLat(36,37,120,121,"Lat"));
                location.setLongitude(StringUtils.randomLonLat(36,37,120,121,"Lon"));

                mLocationCallback.onLocation(location);
                mHandler.postDelayed(this, 3000);
            }
        };

        mHandler.postDelayed(r, 100);

        return null;

    }

    public void stopGps(){
        mHandler.removeCallbacks(r);
    }

    //显示后台定位通知栏（此为8.0版本通知栏）
    private void showNotify() {
        if (channel == null) {
            channel = createNotificationChannel();
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getPackageName());
        Intent intent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("正在后台定位")
                .setContentText("定位进行中")
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

    public class LocalBinder extends Binder {
        public FakeLocationService getFakeLoactionService() {
            //Android O上才显示通知栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showNotify();
            }
            return FakeLocationService.this;
        }
    }

    public interface LocationCallback {
        /**
         * 当前位置
         */
        void onLocation(Location location);

        void onGpsStatue(int status);

        void onGpsChanged(int count,int used);
    }

    private FakeLocationService.LocationCallback mLocationCallback;

    public void setLocationCallback(FakeLocationService.LocationCallback mLocationCallback) {
        this.mLocationCallback = mLocationCallback;

    }






}
