package com.example.root.lyx_android_component.PictureWall;


import android.content.Context;
import android.graphics.Bitmap;
import android.text.Layout;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.root.lyx_android_component.R;

import java.util.HashSet;

/**
 * Created by Yixin Luo on 15-4-7.
 *  照片墙功能的实现，优化了图片载入的方式
 *  adapter实现
 *
 */
public class PictureWallAdapter extends ArrayAdapter<String> implements OnScrollListener {
    /**
     * 图片缓存技术类， 基于最近最少使用
     * */
    private LruCache<String, Bitmap> mMemoryCache;
    /**
     * GridView 实现
     * */
    private GridView mGridView;
    /**
     * 正在或等待下载的任务
     * */
    private Set<BitmapWorkerTask>() mTasks;
    /**
     * 第一张可见图片的坐标
     * */
    private int mFirstVisiblePicturePosition;
    /**
     * 整个屏幕的可见图片数量
     * */
    private int mVisiblePictureInscreen;

    private boolean isFirstEnter = true;

    public PictureWallAdapter(Context context, int resource, String[] objects, GridView gridView) {
        super(context, resource, objects);
        mGridView = gridView;
        mTasks = new HashSet<BitmapWorkerTask>();
        //get max useful memory size
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        //use 1/8 as cache size
        int cacheSize = (int) maxMemory/8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        mGridView.setOnScrollListener(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String url = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.picture_wall_item, null);
        } else {
            view = convertView;
        }
        final ImageView imageView = (ImageView) view.findViewById(R.id.picture_wall_picure);
        imageView.setTag(url);
        setImageView(url, imageView);
        return view;
    }

    private void setImageView(String imageUrl, ImageView  imageView) {
        
    }
}
