package com.wangbj.gpscamera;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;


import android.util.Log;

import com.wangbj.gpscamera.utils.ACache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

/**
 * author:       lans
 * date:         2019-04-1816:13
 * description: 后台定位服务
 **/
public class LocationForegroundService extends Service {
    private static final String TAG = "ForegroundService";
    private LocalBinder localBinder = new LocalBinder();
    private NotificationChannel channel;
    private LocationListener locationListener = new LocationListener(LocationManager.GPS_PROVIDER);
    private GpsListener gpsListener = new GpsListener();
    private LocationManager locationManager;
    private ACache aCache;

    @Override
    public void onCreate() {
        super.onCreate();
        aCache = ACache.get(this);
        Log.e(TAG, "--->onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "--->onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public Location startGps() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        //不加这段话会导致下面爆红,（这个俗称版本压制，哈哈哈哈哈哈）
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {//是否支持Network定位
            //获取最后的network定位信息
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        //每隔1秒请求一次
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.1f, locationListener);
        locationManager.addGpsStatusListener(gpsListener);


        return location;
    }


    public void stopGps(){
        locationManager.removeUpdates(locationListener);
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

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public class LocalBinder extends Binder {
        public LocationForegroundService getLocationForegroundService() {
            //Android O上才显示通知栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showNotify();
            }
            return LocationForegroundService.this;
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

    private LocationCallback mLocationCallback;

    public void setLocationCallback(LocationCallback mLocationCallback) {
        this.mLocationCallback = mLocationCallback;

    }

    private class GpsListener implements android.location.GpsStatus.Listener {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                //第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i(TAG, "第一次定位");
                    break;
                //卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.i(TAG, "卫星状态改变");
                    //获取当前状态
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                    //获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    //创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0; //搜索的所有卫星
                    int used = 0; //正在使用的卫星
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        if (s.usedInFix()) {
                            used++;
                        }
                        count++;
                    }
                    System.out.println("搜索到："+count+"颗卫星");
                    mLocationCallback.onGpsChanged(count,used);
                    break;

                //定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i(TAG, "定位启动");
                    break;
                //定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i(TAG, "定位结束");
                    break;
            }
        }
    }

    private class LocationListener implements android.location.LocationListener {
        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
        }

        @Override
        public void onLocationChanged(Location location) {

            ArrayList arrayList = (ArrayList) aCache.getAsObject("history");
            if (arrayList != null) {
                Double[] doubles = {location.getLatitude(), location.getLongitude()};
                arrayList.add(doubles);
                aCache.put("history", arrayList);
            }
            else{
                ArrayList arrayList1 = new ArrayList();
                Double[] doubles = {location.getLatitude(), location.getLongitude()};
                arrayList1.add(doubles);
                aCache.put("history", arrayList1);
            }


            Log.e(TAG, "onLocationChanged: " + "当前坐标：" + location.getLatitude() + " : " + location.getLongitude());
            if (mLocationCallback != null) {
                mLocationCallback.onLocation(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            mLocationCallback.onGpsStatue(status);
            Log.e(TAG,"onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG,"onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG,"onProviderEnabled");
        }
    }



    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }
}
