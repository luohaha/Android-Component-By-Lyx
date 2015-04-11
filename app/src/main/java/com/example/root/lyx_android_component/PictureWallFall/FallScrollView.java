package com.example.root.lyx_android_component.PictureWallFall;

import android.content.Context;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by root on 15-4-10.
 */
public class FallScrollView extends ScrollView implements OnTouchListener {
    /**
     * 每页要加载的图片数量
     */
    public static final int PAGE_SIZE = 15;

    /**
     * 记录当前已加载到第几页
     */
    private int mPage;

    /**
     * 每一列的宽度
     */
    private int mColumnWidth;

    /**
     * 当前第一列的高度
     */
    private int mFirstColumnHeight;

    /**
     * 当前第二列的高度
     */
    private int mSecondColumnHeight;

    /**
     * 当前第三列的高度
     */
    private int mThirdColumnHeight;

    /**
     * 是否已加载过一次layout，这里onLayout中的初始化只需加载一次
     */
    private boolean isLoadFirstTime;

    /**
     * 对图片进行管理的工具类
     */
    private PictureLoader mPictureLoader;

    /**
     * 第一列的布局
     */
    private LinearLayout mFirstColumn;

    /**
     * 第二列的布局
     */
    private LinearLayout mSecondColumn;

    /**
     * 第三列的布局
     */
    private LinearLayout mThirdColumn;

    /**
     * 记录所有正在下载或等待下载的任务。
     */
    private static Set<LoadImageTask> mTaskCollection;

    /**
     * MyScrollView下的直接子布局。
     */
    private static View scrollLayout;

    /**
     * MyScrollView布局的高度。
     */
    private static int scrollViewHeight;

    /**
     * 记录上垂直方向的滚动距离。
     */
    private static int lastScrollY = -1;

    /**
     * 记录所有界面上的图片，用以可以随时控制对图片的释放。
     */
    private List<ImageView> mImageViewList = new ArrayList<ImageView>();

    public FallScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPictureLoader = PictureLoader.getInstance();
        mTaskCollection = new HashSet<LoadImageTask>();
        setOnClickListener(this);
    }

    private static Handler mHandler = new Handler() {

        public void handleMessage(Message message) {
            FallScrollView fallScrollView = (FallScrollView) message.obj;
            int scrollY = fallScrollView.getScrollY();
            if (scrollY == lastScrollY) {
                if (scrollViewHeight + scrollY >= scrollLayout.getHeight()
                        && mTaskCollection.isEmpty()) {
                    fallScrollView.loadMoreImage();
                }

            } else {
                lastScrollY = scrollY;
                Message message1 = new Message();
                message1.obj = fallScrollView;
                mHandler.sendMessageDelayed(message1, 5);
            }
        };
    };

    
}

