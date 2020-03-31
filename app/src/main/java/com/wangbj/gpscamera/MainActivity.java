package com.wangbj.gpscamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.wangbj.gpscamera.ui.home.HomeFragment;
import com.wangbj.gpscamera.ui.user.UserFragment;
import com.wangbj.gpscamera.ui.path.PathFragment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Fragment> fragments;
    private Fragment fragment;


    /**
     * 双击返回退出程序
     */
    private int clickTime = 0;
    private int times = -1;
    private Timer timer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationBar bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC
                );
        bottomNavigationBar       //定义下面图标及名称及按压颜色
                .addItem(new BottomNavigationItem(R.drawable.ic_home, "首页").setActiveColorResource(R.color.blue))
                .addItem(new BottomNavigationItem(R.drawable.ic_video, "轨迹").setActiveColorResource(R.color.blue))
                .addItem(new BottomNavigationItem(R.drawable.ic_user, "我的").setActiveColorResource(R.color.blue))
                .setFirstSelectedPosition(0)
                .initialise();

        fragments = getFragments();
        fragment = fragments.get(0);
        setDefaultFragment();

        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                if (fragments != null) {

                    for (int i = 0; i < fragments.size(); i++) {
                        ft.hide(fragments.get(i));
                    }

                    fragment = fragments.get(position);
                    ft.show(fragment);
                    ft.commitAllowingStateLoss();

                }
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });

    }

    private void setDefaultFragment() {     //设定默认的主页
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        for (int i = 0; i < fragments.size(); i++) {
            ft.hide(fragments.get(i));
        }

        ft.show(fragments.get(0));
        ft.commit();
    }

    private ArrayList<Fragment> getFragments() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(HomeFragment.newInstance("首页"));
        fragments.add(PathFragment.newInstance("轨迹"));
        fragments.add(UserFragment.newInstance("我的"));
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        transaction.add(R.id.layFrame, fragments.get(0));
        transaction.add(R.id.layFrame, fragments.get(1));
        transaction.add(R.id.layFrame, fragments.get(2));

        transaction.commit();
        return fragments;
    }


    public void onBackPressed() {    //按两次返回退出程序

        clickTime = clickTime + 1;

        if (clickTime == 1 && timer == null) {
            Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    times = times + 1;
                    if (times == 2) {
                        clickTime = 0;
                        times = -1;
                        timer.cancel();
                        timer = null;
                    }
                }
            }, 0, 1000);
        } else if (clickTime == 2) {
            if (timer != null) {
                timer.cancel();
                timer = null;
                super.onBackPressed();
            } else {
                super.onBackPressed();
            }
        }
    }


}

