package com.example.root.lyx_android_component.PictureWallFall;

import android.view.View.OnTouchListener;
import android.widget.ScrollView;

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
    private int page;

    /**
     * 每一列的宽度
     */
    private int columnWidth;

    /**
     * 当前第一列的高度
     */
    private int firstColumnHeight;

    /**
     * 当前第二列的高度
     */
    private int secondColumnHeight;

    /**
     * 当前第三列的高度
     */
    private int thirdColumnHeight;

    /**
     * 是否已加载过一次layout，这里onLayout中的初始化只需加载一次
     */
    private boolean loadOnce;

    /**
     * 对图片进行管理的工具类
     */
    private ImageLoader imageLoader;

    /**
     * 第一列的布局
     */
    private LinearLayout firstColumn;

    /**
     * 第二列的布局
     */
    private LinearLayout secondColumn;

    /**
     * 第三列的布局
     */
    private LinearLayout thirdColumn;

    /**
     * 记录所有正在下载或等待下载的任务。
     */
    private static Set<LoadImageTask> taskCollection;

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
    private List<ImageView> imageViewList = new ArrayList<ImageView>();

    Handler mHandler = new Handler() {
        @Override
        public void close() {

        }

        @Override
        public void flush() {

        }

        @Override
        public void publish(LogRecord record) {

        }
    };
}
