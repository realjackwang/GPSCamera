package com.wangbj.gpscamera.ui.path;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.ListFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Response;

import com.alibaba.fastjson.JSONArray;
import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.bean.Path;
import com.wangbj.gpscamera.httpservice.JsonService;
import com.wangbj.gpscamera.httpservice.TestService;
import com.wangbj.gpscamera.utils.ACache;
import com.wangbj.gpscamera.utils.ArrayListUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {
    private Context context;
    private ArrayList pathList;
    private SimpleAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ACache aCache;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_path, container, false);
        initView(root);

        context = getContext();
        aCache = ACache.get(context);
        swipeRefreshLayout.setOnRefreshListener(this);

        refreshList();

        return root;
    }

    private void initView(View root) {
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh);
    }


    private void refreshList() {
        swipeRefreshLayout.setRefreshing(true);
        ArrayList<Path> arrayList = (ArrayList<Path>) aCache.getAsObject("PathHistory");
        if (arrayList != null) {
            pathList = arrayList;
            adapter = new PathListAdapter(getContext(),
                    pathList,
                    R.layout.path_listviewitem,
                    new String[]{"title", "info", "starttime", "endtime"},
                    new int[]{R.id.title, R.id.info, R.id.starttime, R.id.endtime},
                    false);
            setListAdapter(adapter);
        }
        swipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e("PathFragment", "onHiddenChanged");
        if (!hidden) {
            refreshList();
        }
    }


    public static PathFragment newInstance(String content) {
        Bundle args = new Bundle();
        args.putString("ARGS", content);
        PathFragment fragment = new PathFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        final Path mItemPathArr = (Path) l.getItemAtPosition(position);
        final String mItemPathId = (String) mItemPathArr.get("id");
        final int mItemPlaceId = (int) mItemPathArr.get("place");

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        final ImageView ivCloud = v.findViewById(R.id.iv_cloud);

        String[] baseChoices = {"查看路径", "下载视频", "分享路径", "删除路径"};
        List<String> testList = Arrays.asList(baseChoices);
        ArrayList<String> arrayList = new ArrayList(testList);
        if (ivCloud.getVisibility() == View.GONE) {
            arrayList.add("上传");
        }
        builder.setTitle("选择一个选项");
        final String[] choices = arrayList.toArray(new String[0]);
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent();
                        intent.putExtra("id", mItemPathId);
                        intent.setClass(getActivity(), PathDetailActivity.class);
                        getActivity().startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.activity_bottom_enter, R.anim.activity_stay);
                        break;
                    case 1:
                        Log.e("onListItemClick", "下载视频");

                        TestService.test((Path) pathList.get(position), new TestService.OnHttpListener() {
                            @Override
                            public void onHttpSuccess(Response response) {

                            }

                            @Override
                            public void onHttpFailed(Exception e) {

                            }
                        });


                        break;
                    case 2:
                        Log.e("onListItemClick", "分享路径");
                        break;
                    case 3:
                        ArrayList<Path> newPathArr = (ArrayList<Path>) aCache.getAsObject("PathHistory");
                        Path path = new Path();
                        for (int i = 0; i < newPathArr.size(); i++) {
                            if (newPathArr.get(i).get("id").equals(mItemPathId)) {
                                path = newPathArr.get(i);
                                break;
                            }
                        }
                        newPathArr.remove(path);
                        aCache.put("PathHistory", newPathArr);
                        if(ivCloud.getVisibility() == View.VISIBLE) {


                            JSONArray jsonArray = new JSONArray();
                            jsonArray.add(mItemPathId);

                            JsonService.Send(aCache, "/del_path/", jsonArray, new JsonService.OnHttpListener() {
                                @Override
                                public void onHttpSuccess(final Response response) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String str = null;
                                            try {
                                                str = response.body().string();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(str);
                                            if ((int) jsonObject.get("code") == 200) {
                                                Toast.makeText(context, jsonObject.get("msg").toString(), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(context, jsonObject.get("msg").toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onHttpFailed(Exception e) {

                                }
                            });
                        }

                        refreshList();
                        break;
                    case 4:
                        Log.e("onListItemClick", "上传");

                        JSONArray jsonArray = new JSONArray();
                        jsonArray.add(mItemPathArr);
                        JsonService.Send(aCache, "/upload_path/", jsonArray, new JsonService.OnHttpListener() {
                            @Override
                            public void onHttpSuccess(final Response response) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String str = null;
                                        try {
                                            str = response.body().string();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(str);
                                        if ((int) jsonObject.get("code") == 200) {
                                            Toast.makeText(context, jsonObject.get("msg").toString(), Toast.LENGTH_SHORT).show();
                                            ArrayList<Path> cachePathArr = (ArrayList<Path>) aCache.getAsObject("PathHistory");
                                            ArrayList<Path> newPathArr = ArrayListUtils.changeCloud(cachePathArr, mItemPathId);
                                            aCache.put("PathHistory", newPathArr);
                                            refreshList();
                                        } else {
                                            Toast.makeText(context, jsonObject.get("msg").toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onHttpFailed(Exception e) {

                            }
                        });

                        break;


                }
            }
        });
        builder.show();

        super.onListItemClick(l, v, position, id);
    }


    @Override
    public void onRefresh() {
        Toast.makeText(getActivity(), "刷新", Toast.LENGTH_SHORT).show();//刷新时要做的事情
        swipeRefreshLayout.setRefreshing(false);//刷新完成
    }
}

