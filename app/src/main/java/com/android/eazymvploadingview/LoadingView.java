package com.android.eazymvploadingview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Group;

import com.cunoraz.gifview.library.GifView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class LoadingView extends ConstraintLayout {
    /**
     * 背景透明
     */
    public static final int BG_MODE_TRANSPARENT = 1;
    /**
     * 背景不透明
     */
    public static final int BG_MODE_OPAQUE = 2;
    /**
     * 重试按钮的回调
     */
    private RetryCallBack mRetryCallBack;
    /**
     * 取消加载的监听对象
     */
    private OnCancelListener mCloseListener;

    /**
     * 父布局对象
     */
    private ViewGroup mParentViewGroup;
    /**
     * 加载页面的容器,用于显示隐藏加载
     */
    private LinearLayout mLoadingLayout;
    /**
     * 错误信息的容器,用于显示隐藏错误信息是否显示
     */
    private Group mErrorGroup;
    /**
     * 加载的图片
     */
    private GifView mGifView;
    /**
     * 错误信息
     */
    private TextView mTvErrorMsg;
    /**
     * 重试按钮
     */
    private TextView mBtnRetry;

    /**
     * 当前的mode类型
     */
    private int mCurMode;

    /**
     * 是否可以撤销的判断
     */
    private boolean isCancelAble;
    /**
     * 非透明时的背景颜色
     */
    private int opaqueModeBackgroundColor;
    /**
     * 是否有被隐藏的页面 如果有会在加载完成的时候显示出来
     */
    private List<View> invisibles;
    /**
     * 是否错误
     */
    private boolean isError;

    /**
     * 加载图片
     */
    private ImageView mDateStatus;

    /**
     * 全占满
     */
    private ViewGroup.LayoutParams matchParentLayoutParams = new LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);

    /**
     * 定义一个限制注解,让方法的传入值只能是我限定的值
     */
    @IntDef({BG_MODE_TRANSPARENT, BG_MODE_OPAQUE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LoadingMode {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //事件分发,在这里拦截掉事件继续往下发送让弹窗时无法点击被遮挡的背景功能
        return true;
    }

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化一个加载控件
     *
     * @param group
     * @return
     */
    public static LoadingView injectViewGroup(ViewGroup group) {
        for (int i = 0 ; i < group.getChildCount() ; i++) {
            if (group.getChildAt(i) instanceof LoadingView) {
                return (LoadingView) group.getChildAt(i);
            }
        }
        LoadingView loadingView = (LoadingView) LayoutInflater
                .from(group.getContext())
                .inflate(R.layout.layout_loading, null);

        loadingView.mParentViewGroup = group;

        return loadingView;
    }

    /**
     * 在这里加载控件并设置重试按钮的点击监听
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mLoadingLayout = findViewById(R.id.loading_load_layout);

        mErrorGroup = findViewById(R.id.loading_error_layout);

        mGifView = findViewById(R.id.loading_gif_view);
        gifPause();//找到控件先让动画停止避免浪费内存

        mTvErrorMsg = findViewById(R.id.loading_tv_error_msg);

        mBtnRetry = findViewById(R.id.loading_btn_retry);

        mDateStatus = findViewById(R.id.imageView);

        //重试按钮的点击监听
        mBtnRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRetryCallBack != null) {
                    mRetryCallBack.onRetry();
                    if (mCurMode == BG_MODE_OPAQUE) {
                        mDateStatus.setVisibility(GONE);
                        isError = false;
                        show(mCurMode);
                    }
                }
            }
        });
    }

    /**
     * 设置网络请求失败和没数据
     */
    public void setDateNetStatue(@DrawableRes int drawarbleResId) {
        mDateStatus.setBackgroundResource(drawarbleResId);
    }

    /**
     * 设置加载图标的样式
     *
     * @param gifDrawarbleResId gif图片的id
     */
    public void setGifIcon(@IdRes int gifDrawarbleResId) {
        mGifView.setGifResource(gifDrawarbleResId);
    }

    /**
     * 设置加载页面的背景
     *
     * @param drawarbleResId 背景图片最好是.9的
     */
    public void setLoadingBackground(@DrawableRes int drawarbleResId) {
        mLoadingLayout.setBackgroundResource(drawarbleResId);
    }

    /**
     * 设置加载页面的背景
     *
     * @param drawarble 背景图片最好是.9的
     */
    public void setLoadingBackground(Drawable drawarble) {
        mLoadingLayout.setBackground(drawarble);
    }

    /**
     * 设置重试按钮的样式
     *
     * @param styleResId 样式资源id
     */
    public void setRetryBtnStyle(@DrawableRes int styleResId) {
        mBtnRetry.setBackgroundResource(styleResId);
    }

    /**
     * 非透明 背景颜色
     *
     * @param colorRes
     */
    public void setModeBackgroundColor(@ColorRes int colorRes) {
        Resources resources = getContext().getResources();
        if (resources != null) {
            opaqueModeBackgroundColor = resources.getColor(colorRes);
        } else {
            opaqueModeBackgroundColor = Color.WHITE;
        }
    }

    /**
     * 非透明 背景颜色
     *
     * @param colorString
     */
    public void setModeBackgroundColor(String colorString) {
        opaqueModeBackgroundColor = Color.parseColor(colorString);
    }

    /**
     * 获取非透明 背景颜色
     *
     * @return
     */
    private int getModeBackgroundColor() {
        if (opaqueModeBackgroundColor == 0) {
            return Color.WHITE;
        }
        return opaqueModeBackgroundColor;
    }

    /**
     * 显示loading页面
     *
     * @param mode
     */
    private void show(@LoadingMode int mode) {
        //把mode添加到当前curMode用于之后的判断,判断mode类型显示相对应的背景样式
        mCurMode = mode;
        switch (mCurMode) {
            case BG_MODE_OPAQUE:
                setBackgroundColor(getModeBackgroundColor());
                break;
            case BG_MODE_TRANSPARENT:
                setBackgroundColor(Color.TRANSPARENT);
                break;
        }
        //隐藏error状态下的控件,把加载的控件显示出来
        mErrorGroup.setVisibility(GONE);
        mLoadingLayout.setVisibility(VISIBLE);

        //设置返回键监听
        setBackListener();

        //播放动画
        gifPlay();
    }

    private void setBackListener() {
        //在加载界面显示后给加载界面设置聚焦,并添加返回键监听
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (!isError) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                        //判断是不是显示状态,然后判断是否可以取消加载,并且背景是透明的
                        if (isShown()) {
                            if (isCancelAble && mCurMode == BG_MODE_TRANSPARENT) {
                                loadingClose();
                            }
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    @SuppressLint("NewApi")
    public void loadingClose() {
        gifPause();
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        } else {
            if (mParentViewGroup != null) {
                mParentViewGroup.removeView(this);
            }
        }
        if (invisibles != null && invisibles.size() > 0) {
            for (View gone : invisibles) {
                gone.setVisibility(VISIBLE);
                if (gone instanceof ViewGroup) {
                    gone.scrollTo(0, 0);
                }
            }
            invisibles.clear();
        }
    }

    /**
     * 显示 加载错误页面
     *
     * @param msg
     * @param callBack
     */
    public void onError(String msg, RetryCallBack callBack) {
        isError = true;
        if (mCurMode == BG_MODE_TRANSPARENT) {
            loadingClose();
            return;
        }
        mDateStatus.setVisibility(VISIBLE);
        mLoadingLayout.setVisibility(GONE);
        mErrorGroup.setVisibility(VISIBLE);
        if (!isOnInternet(getContext())) {
            mTvErrorMsg.setText("网络连接失败,请确认网络状态!");
        } else {
            if (TextUtils.isEmpty(msg)) {
                mTvErrorMsg.setText("服务器貌似宕机了~!");
            } else {
                mTvErrorMsg.setText(msg);
            }
        }
        mRetryCallBack = callBack;
    }

    /**
     * 播放 帧或gif 动画
     */
    private void gifPlay() {
        mGifView.play();
    }

    /**
     * 暂停 帧或gif 动画
     */
    private void gifPause() {
        mGifView.pause();
    }

    /**
     * showLoading的重载方法在这个方法中实现了loding的显示功能
     */
    public void showLoading(@LoadingMode int mode) {
        //判断并添加loading页到这个容器中
        isAddViewGroup();
        //判断显示模式,看看是隐藏底部内容的模式还是透明模式
        this.show(mode);
    }

    /**
     * 判断并添加loading页到注入的容器中
     */
    @SuppressLint("ResourceType")
    private void isAddViewGroup() {
        //判断这个容器是哪个类型的,如果是帧布局和相对布局直接添加即可
        if (mParentViewGroup instanceof FrameLayout || mParentViewGroup instanceof RelativeLayout) {
            if (mParentViewGroup instanceof ScrollView || mParentViewGroup.getClass().getName().contains("ScrollView")) {
                View childAt = mParentViewGroup.getChildAt(0);
                if (childAt instanceof ViewGroup) {
                    if (childAt instanceof ConstraintLayout) { //如果是约束布局就得设置约束方向
                        if (this.getParent() == null) {
                            ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                            mParentViewGroup.removeView(childAt);
                            FrameLayout frameLayout =
                                    new FrameLayout(mParentViewGroup.getContext());
                            frameLayout.setLayoutParams(layoutParams);
                            frameLayout.addView(childAt);
                            if (layoutParams.height < getScreenHeight(getContext())) {
                                childAt.setVisibility(INVISIBLE);
                                if (invisibles == null) {
                                    invisibles = new ArrayList<>();
                                }
                                invisibles.add(childAt);
                                frameLayout.addView(this,
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        getScreenHeight(getContext()));
                            } else {
                                frameLayout.addView(this,
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT);
                            }
                            mParentViewGroup.addView(frameLayout);
                        }
                    } else {
                        if (this.getParent() == null) {
                            ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                            if (layoutParams.height < getScreenHeight(getContext())) {
                                childAt.setVisibility(INVISIBLE);
                                if (invisibles == null) {
                                    invisibles = new ArrayList<>();
                                }
                                invisibles.add(childAt);
                                ((ViewGroup) childAt).addView(this,
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        getScreenHeight(getContext()));
                            } else {
                                ((ViewGroup) childAt).addView(this,
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT);
                            }
                            mParentViewGroup = (ViewGroup) childAt;
                        }
                    }
                } else {
                    if (this.getParent() == null) {
                        ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                        mParentViewGroup.removeView(childAt);
                        FrameLayout frameLayout = new FrameLayout(mParentViewGroup.getContext());
                        frameLayout.setLayoutParams(layoutParams);
                        frameLayout.addView(childAt);
                        frameLayout.addView(this,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                        mParentViewGroup.addView(frameLayout);
                    }
                }
            } else {
                if (mParentViewGroup != getParent()) {
                    setLayoutParams(matchParentLayoutParams);
                    mParentViewGroup.addView(this);
                }
            }
        } else if (mParentViewGroup instanceof ConstraintLayout) { //如果是约束布局就得设置约束方向
            if (this.getParent() == null) {
                //创建一个约束布局的设置类
                ConstraintSet constraintSet = new ConstraintSet();
                if (this.getId() == -1) {
                    this.setId(0x3333);
                }
                //把这个约束布局添加到设置类中
                constraintSet.clone((ConstraintLayout) mParentViewGroup);

                //把loding控件约束到容器的上下左右各个方向上占满整个屏幕
                constraintSet.connect(this.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP);
                constraintSet.connect(this.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID,
                        ConstraintSet.LEFT);
                constraintSet.connect(this.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID,
                        ConstraintSet.RIGHT);
                constraintSet.connect(this.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID,
                        ConstraintSet.BOTTOM);

                //设置为全占满
                constraintSet.constrainWidth(this.getId(), ConstraintSet.MATCH_CONSTRAINT);
                constraintSet.constrainHeight(this.getId(), ConstraintSet.MATCH_CONSTRAINT);

                //添加到约束布局
                mParentViewGroup.addView(this);
                //提交修改
                constraintSet.applyTo((ConstraintLayout) mParentViewGroup);
            }
        } else {//如果是未知的容器则使用通用方法
            if (this.getParent() == null) {
                //创建一个帧布局,然后把原来容器的所有属性给予这个帧布局
                ViewGroup parent = (ViewGroup) mParentViewGroup.getParent();
                FrameLayout root = new FrameLayout(parent.getContext());
                root.setLayoutParams(mParentViewGroup.getLayoutParams());
                //从原父布局上删除掉这个容器
                parent.removeView(mParentViewGroup);
                //把这个容器添加到新创建的帧布局中 设置全占满,因为以前的容器要和现在的容器一样大
                root.addView(mParentViewGroup,
                        new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT));
                //把容器添加到帧布局中,然后把帧布局添加到根布局中
                root.addView(this);
                parent.addView(root);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public static boolean isOnInternet(Context context) {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mCloseListener != null) {
            mCloseListener.onCancel();
        }
    }

    public void setCancelListener(OnCancelListener closeListener) {
        this.mCloseListener = closeListener;
    }

    public boolean isShow() {
        return getParent() != null;
    }

    public interface RetryCallBack {
        void onRetry();
    }


    public interface OnCancelListener {
        void onCancel();
    }

}
