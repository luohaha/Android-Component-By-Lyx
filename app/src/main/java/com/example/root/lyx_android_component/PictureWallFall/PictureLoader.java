package com.example.root.lyx_android_component.PictureWallFall;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

/**
 * Created by Yixin on 15-4-9.
 */
public class PictureLoader {
    //init a cache class
    private LruCache<String, Bitmap> mMemoryCache;
    //the only PictureLoader
    private static PictureLoader mPictureLoader;
    //max memory size the app can use
    private int mMaxMemory;
    //the max cache's size
    private int mMaxCache;

    /**
     * new one
     */
    private PictureLoader() {
        mMaxMemory = (int) Runtime.getRuntime().maxMemory();
        mMaxCache = mMaxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(mMaxCache) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    /**
     * make sure that it will have only one picture loader exit
     * @return the only one loader
     */
    public static PictureLoader getInstance() {
        if (mPictureLoader == null) {
            mPictureLoader = new PictureLoader();
        }
        return mPictureLoader;
    }

    /**
     * if there is not a bitmap in cache, then put it into cache
     * @param key
     * @param value
     */
    private void addBitmapToCache(String key, Bitmap value) {
        if (getBitmapFromCache(key) == null) {
            mMemoryCache.put(key, value);
        }
    }

    /**
     * return the bitmap which key is key
     * @param key
     * @return
     */
    private Bitmap getBitmapFromCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * calculate the InSample Size using the radio of requestWidth and options.outwidth
     * @param options get the width of picture
     * @param requestWidth the width we want
     * @return the bitmapfactory's options' InSampleSize
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int requestWidth) {
        int width = options.outWidth;
        int InSampleSize = 1;
        if (requestWidth < width) {
            InSampleSize = Math.round(width / requestWidth);
        }
        return InSampleSize;
    }

    
}
