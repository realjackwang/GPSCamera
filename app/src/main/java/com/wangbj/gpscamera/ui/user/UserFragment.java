package com.wangbj.gpscamera.ui.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import okhttp3.Response;

import com.alibaba.fastjson.JSONArray;
import com.eminayar.panter.DialogType;
import com.eminayar.panter.PanterDialog;
import com.eminayar.panter.interfaces.OnTextInputConfirmListener;
import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.bean.Path;
import com.wangbj.gpscamera.httpservice.JsonService;
import com.wangbj.gpscamera.utils.ACache;

import java.io.IOException;
import java.util.ArrayList;


public class UserFragment extends Fragment {

    private Context context;
    private LinearLayout ui_server_url,ui_btn_server_url;
    private ACache aCache;
    private SharedPreferences sp;
    private TextView ui_settings, ui_phone, ui_access,ui_path_index;

    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user, container, false);
        context = getContext();
        aCache = ACache.get(context);
        sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        ui_server_url = root.findViewById(R.id.server_url);
        ui_btn_server_url = root.findViewById(R.id.btn_server_url);
        ui_settings = root.findViewById(R.id.settings);
        ui_access = root.findViewById(R.id.user_type);
        ui_phone = root.findViewById(R.id.phone);
        ui_path_index = root.findViewById(R.id.path_index);

        String access = sp.getString("ACCESS", "");
        changeAccessLabel(access);

        ArrayList<Path> arrayList = (ArrayList<Path>) aCache.getAsObject("PathHistory");
        if (arrayList != null) {
            ui_path_index.setText(String.valueOf(arrayList.size()));
        }

        ui_phone.setText(sp.getString("USERNAME", ""));
        ui_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SettingsActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.activity_enter, R.anim.activity_stay);
            }
        });

        ui_btn_server_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2020-03-25 bug：当访问其他能够正常访问的网页时也会修改成功，需要添加特定标识符，非紧急
                new PanterDialog(context)
                        .setHeaderBackground(R.drawable.pattern_bg_blue)
                        .setHeaderLogo(R.drawable.ic_logo)
                        .setDialogType(DialogType.INPUT)
                        .isCancelable(true)
                        .input("服务器地址",
                                "请填写正确的服务器地址", new
                                        OnTextInputConfirmListener() {
                                            @Override
                                            public void onTextInputConfirmed(final String text) {
                                                final String server = aCache.getAsString("Server");
                                                aCache.put("Server", text);
                                                JsonService.Send(aCache, "/ping/", new JSONArray(), new JsonService.OnHttpListener() {
                                                    @Override
                                                    public void onHttpSuccess(Response response) throws IOException {
                                                        aCache.put("Server", text);
                                                        Looper.prepare();
                                                        Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
                                                        Looper.loop();
                                                    }

                                                    @Override
                                                    public void onHttpFailed(Exception e) {
                                                        aCache.put("Server", server);
                                                        if (e instanceof IllegalArgumentException) {
                                                            Toast.makeText(context, "无法连接服务器地址，修改失败", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Looper.prepare();
                                                            Toast.makeText(context, "无法连接服务器地址，修改失败", Toast.LENGTH_SHORT).show();
                                                            Looper.loop();
                                                        }
                                                    }
                                                });
                                            }
                                        })
                        .show();
            }
        });

        return root;
    }

    private void changeAccessLabel(String access) {
        switch (access) {
            case "0":
                ui_access.setText(R.string.user_access_admin);
                ui_access.setBackgroundColor(getResources().getColor(R.color.orange));
                ui_access.setVisibility(View.VISIBLE);

                ui_server_url.setVisibility(View.VISIBLE);
                break;
            case "1":
                ui_access.setText(R.string.user_access_enterprise);
                ui_access.setBackgroundColor(getResources().getColor(R.color.teal));
                ui_access.setVisibility(View.VISIBLE);

                ui_server_url.setVisibility(View.GONE);
                break;
            case "2":
                ui_access.setVisibility(View.GONE);

                ui_server_url.setVisibility(View.GONE);
                break;
        }
    }


    public static UserFragment newInstance(String content) {

        Bundle args = new Bundle();
        args.putString("ARGS", content);
        UserFragment fragment = new UserFragment();
        fragment.setArguments(args);
        return fragment;
    }

}