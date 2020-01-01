package com.wangbj.gpscamera.ui.path;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.bean.Path;
import com.wangbj.gpscamera.utils.ACache;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class PathFragment extends ListFragment {

    protected WeakReference<View> mRootView;
    private ListView listView;
    private ArrayList pathlist;
    private SimpleAdapter adapter;

    private ACache aCache;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        if (mRootView == null || mRootView.get() == null) {
            View view = inflater.inflate(R.layout.fragment_dashboard, null);
            mRootView = new WeakReference<View>(view);
        } else {
            ViewGroup parent = (ViewGroup) mRootView.get().getParent();
            if (parent != null) {
                parent.removeView(mRootView.get());
            }
        }

        aCache = ACache.get(getContext());

        ArrayList<Path> arrayList  =  (ArrayList<Path>) aCache.getAsObject("PathHistory");

        if (arrayList != null){
            pathlist = arrayList;
            adapter = new SimpleAdapter(getContext(),
                    pathlist, R.layout.path_listviewitem,
                    new String[]{"title", "info","starttime","endtime"},
                    new int[]{R.id.title, R.id.info,R.id.starttime,R.id.endtime});
            setListAdapter(adapter);
        }

        return mRootView.get();
    }




    public static PathFragment newInstance(String content) {
        Bundle args = new Bundle();
        args.putString("ARGS", content);
        PathFragment fragment = new PathFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Path map=(Path) l.getItemAtPosition(position);
        final long Text= (long)map.get("id");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);

        builder.setTitle("选择一个选项");
        //    指定下拉列表的显示数据
        final String[] cities = {"查看路径", "下载视频", "分享路径","删除路径"};
        //    设置一个下拉的列表选择项
        builder.setItems(cities, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which){
                    case 0:
                        Intent intent = new Intent();
                        intent.putExtra("id", Text);
                        intent.setClass(getActivity(), PathDetail.class);
                        getActivity().startActivity(intent);
                        break;
                    case 1:
                        Log.e("onListItemClick","下载视频");
                        break;
                    case 2:
                        Log.e("onListItemClick","分享路径");
                        break;
                    case 3:
                        Log.e("onListItemClick","删除路径");
                        break;


                }
            }
        });
        builder.show();


        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
    }


}

