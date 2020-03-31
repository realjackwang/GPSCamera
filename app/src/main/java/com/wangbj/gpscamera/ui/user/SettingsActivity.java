package com.wangbj.gpscamera.ui.user;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.ui.login.LoginActivity;
import com.wangbj.gpscamera.ui.user.settings.AccountSettingsActivity;
import com.wangbj.gpscamera.utils.ACache;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
    }

    public void show_privacy_note(View view){
        Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
    }

    public void app_info(View view) {
        Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
//        startActivity(new Intent(Settings.this, Appinfo.class));
    }

    public void feedback(View view) {
        Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
//        startActivity(new Intent(Settings.this, Feedback.class));
    }

    public void open_source(View view) {
        Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
//        startActivity(new Intent(Settings.this, Support.class));
    }

    public void check_update(View view) {
        Toast.makeText(this, "检查失败", Toast.LENGTH_SHORT).show();
//        startActivity(new Intent(Settings.this, Support.class));
    }

    public void user_settings(View view){
        startActivity(new Intent(SettingsActivity.this, AccountSettingsActivity.class));
    }

    public void quit_account(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 添加按钮的单击事件
        // 设置显示信息
        // 单击事件
        builder.setTitle("退出当前账号");
        builder.setMessage("退出后你将无法记录新的轨迹，你的所有本地轨迹也将一并清除，确定退出？")
                .
                // 设置确定按钮
                        setPositiveButton("确定退出",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final SharedPreferences sp = SettingsActivity.this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.clear();
                                editor.apply();

                                ACache aCache = ACache.get(SettingsActivity.this);
                                aCache.clear();
                                SettingsActivity.this.startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                                SettingsActivity.this.finish();

                            }
                        }).
                // 设置取消按钮
                        setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
        // 创建对话框
        AlertDialog ad = builder.create();
        // 显示对话框
        ad.show();
    }

    public void back(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_stay, R.anim.activity_sub_exit);
    }


}
