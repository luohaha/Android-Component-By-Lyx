package com.example.root.lyx_android_component.PictureWall;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.root.lyx_android_component.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

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
    private Set<BitmapWorkerTask> mTasks;
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
        int maxMemory = (int) (Runtime.getRuntime().maxMemory()/8);
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

    /**
     * set a picture for imageView
     * @param imageUrl key of LruCache
     * @param imageView the widget used to show this picture
     */
    private void setImageView(String imageUrl, ImageView  imageView) {
        Bitmap bitmap = getBitmapFromCache(imageUrl);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher);
        }
    }

    /**put the bitmap into the mMemoryCache
     * @param key
     * @param value
     */
    private void addBitmapToCache(String key, Bitmap value) {
        if (getBitmapFromCache(key) == null) {
            mMemoryCache.put(key, value);
        }
    }

    /**
     * get a bitmap form memory cache when url equal to key
     * @param key the bitmap's key which we want to get
     * @return the bitmap if it's key equal to key
     */
    private Bitmap getBitmapFromCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * get the gridview's status, when gridview is rest and load pictures, else
     * stop down load pictures
     * @param view
     * @param scrollState
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            //down load the picure when stop
            loadBitmap(mFirstVisiblePicturePosition, mVisiblePictureInscreen);
        } else {
            //when it scroll, stop load bitmap
            cancelAllTasks();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        mFirstVisiblePicturePosition = firstVisibleItem;
        mVisiblePictureInscreen = visibleItemCount;
        //when we start the app first time, we should load pictures
        if (isFirstEnter && visibleItemCount > 0) {
            loadBitmap(firstVisibleItem, visibleItemCount);
            isFirstEnter = false;
        }
    }

    /**
     * if the bitmap is not in cache, then create a new thread to down load it
     * @param firstVisibleItem
     * @param visibleItemCount
     */
    private void loadBitmap(int firstVisibleItem, int visibleItemCount) {
        try {
            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                String imageUrl = Images.pictures[i];
                Bitmap bitmap = getBitmapFromCache(imageUrl);
                if (bitmap == null) {
                    BitmapWorkerTask task = new BitmapWorkerTask();
                    mTasks.add(task);
                    task.execute(imageUrl);
                } else {
                    ImageView imageView = (ImageView) mGridView.findViewWithTag(imageUrl);
                    if (imageView != null && bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void cancelAllTasks() {
        if (mTasks != null) {
            for (BitmapWorkerTask task : mTasks) {
                task.cancel(false);
            }
        }
    }

    /**
     *
     */
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private String imageUrl;

        /**start a download task in background
         * @param params the url
         * @return
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            imageUrl = params[0];
            Bitmap bitmap = downloadBitmap(params[0]);
            if (bitmap != null) {
                addBitmapToCache(params[0], bitmap);
            }
            return bitmap;
        }

        /**
         * find the imageview which is connected to a bitmap using tag.
         * and show the bitmap in this imageview
         * @param bitmap the bitmap we want to show in imageview
         */
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            ImageView imageView = (ImageView) mGridView.findViewWithTag(imageUrl);
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            mTasks.remove(this);
        }
        /**
         * open a http connection and down load a picture from internet
         * @param imageUrl the url of picture we want to download
         * @return the bitmap what we get
         */
        private Bitmap downloadBitmap(String imageUrl) {
            Bitmap bitmap = null;
            HttpURLConnection httpURLConnection = null;
            InputStream is = null;
            InputStream iss = null;
            try {
                //create a new input stream
<<<<<<< HEAD
               // InputStream is = getInputStreamFromHttp(imageUrl, 5000, 10000);
                URL url = new URL(imageUrl);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setReadTimeout(10000);
                is = httpURLConnection.getInputStream();
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;


=======
                InputStream is = getInputStreamFromHttp(httpURLConnection, imageUrl, 5000, 10000);
                BitmapFactory.Options options = new BitmapFactory.Options();
>>>>>>> cc38c1dbba3c6e187b992af8d7e7383987c1beb0
                BitmapFactory.decodeStream(is, null, options);
                options.inSampleSize = calculateInSampleSize(options, 100, 100);
                options.inJustDecodeBounds = false;
                is.close();
                //create a new input stream again, or you can not down load this picture
<<<<<<< HEAD
              //  InputStream iss = getInputStreamFromHttp(imageUrl, 5000, 10000);
                url = new URL(imageUrl);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setReadTimeout(10000);
                iss = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(iss, null, options);
                iss.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        /**
         * bug : this function can not work well.
         *       So I delete it.
         * get input stream from http connection, using imageUrl and setting connection timeout and
         * read time out
         * @param httpURLConnection using a http connection to finish the job
         * @param imageUrl the image's url
         * @param connectTimeOut http's connection timeout which you want to set
         * @param readTimeOut
         * @return
         */
        /*
        private InputStream getInputStreamFromHttp(
              String imageUrl, int connectTimeOut, int readTimeOut) {
            InputStream iss = null;
            HttpURLConnection httpURLConnection = null;
            try {
                URL url = new URL(imageUrl);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(connectTimeOut);
                httpURLConnection.setReadTimeout(readTimeOut);
                iss = httpURLConnection.getInputStream();
=======
                InputStream iss = getInputStreamFromHttp(httpURLConnection, imageUrl, 5000, 10000);
                bitmap = BitmapFactory.decodeStream(iss, null, options);
                iss.close();
>>>>>>> cc38c1dbba3c6e187b992af8d7e7383987c1beb0
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return iss;
        }
        */
        /**
         * get input stream from http connection, using imageUrl and setting connection timeout and
         * read time out
         * @param httpURLConnection using a http connection to finish the job
         * @param imageUrl the image's url
         * @param connectTimeOut http's connection timeout which you want to set
         * @param readTimeOut
         * @return
         */
        private InputStream getInputStreamFromHttp(HttpURLConnection httpURLConnection,
              String imageUrl, int connectTimeOut, int readTimeOut) {
            InputStream iss = null;
            try {
                URL url = new URL(imageUrl);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(connectTimeOut);
                httpURLConnection.setReadTimeout(readTimeOut);
                iss = httpURLConnection.getInputStream();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return iss;
        }
        /**
         * calculate the radio using request width and height
         * @param options which we want to get the old height and width from
         * @param requestWidth
         * @param requestHeight
         * @return
         */
        private int calculateInSampleSize(BitmapFactory.Options options, int requestWidth,
                                                 int requestHeight) {
            //get the old height and width
            final int height = options.outHeight;
            final int width = options.outWidth;

            int inSampleSize = 1;
            //not suitable
            if (height > requestHeight || width > requestWidth) {
                final int heightRadio = Math.round((float) height / (float) requestHeight);
                final int widthRadio = Math.round((float) width / (float) requestWidth);
                inSampleSize = (heightRadio < widthRadio) ? heightRadio : widthRadio;
            }
            return inSampleSize;
        }

    }

}
