package com.wangbj.gpscamera.ui.path;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.wangbj.gpscamera.R;
import com.wangbj.gpscamera.bean.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author JackWang
 * @fileName PathListAdapter
 * @date on 2020-03-28 下午 12:43
 * @email 544907049@qq.com
 **/

public class PathListAdapter extends SimpleAdapter {
    /**
     * Constructor
     *
     * @param context  The context where the View associated with this SimpleAdapter is running
     * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
     *                 Maps contain the data for each row, and should include all the entries specified in
     *                 "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *                 item. The layout file should include at least those named views defined in "to"
     * @param from     A list of column names that will be added to the Map associated with each
     *                 item.
     * @param to       The views that should display column in the "from" parameter. These should all be
     *                 TextViews. The first N views in this list are given the values of the first N columns
     */

    private ArrayList<Path> pathArr;

    public PathListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to,boolean isCloud) {
        super(context, data, resource, from, to);
        this.pathArr = (ArrayList<Path>) data;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        ImageView mImgCloud = view.findViewById(R.id.iv_cloud);   //获取姓名容器
        if ((boolean)this.pathArr.get(position).get("cloud")){
            mImgCloud.setVisibility(View.VISIBLE);
        }else{
            mImgCloud.setVisibility(View.GONE);
        }
        return view;
    }


}
