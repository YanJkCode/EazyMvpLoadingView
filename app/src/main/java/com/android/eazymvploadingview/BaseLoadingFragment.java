package com.android.eazymvploadingview;

import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;

import com.android.eazymvp.base.baseInterface.IBasePresenter;
import com.android.eazymvp.base.baseimpl.view.BaseMvpFragment;


public abstract class BaseLoadingFragment<P extends IBasePresenter> extends BaseMvpFragment<P> {

    private LoadingView mLoadingView;
    private ViewGroup mGroup;
    /**
     * 显示loding页面
     */
    protected void showLoading() {
        showLoading(LoadingView.BG_MODE_TRANSPARENT);
    }

    /**
     * 显示loding页面
     */
    protected void showLoading(@LoadingView.LoadingMode int mode) {
        if (mGroup == null) {
            showLoading(mode, (ViewGroup) getView());
        } else {
            showLoading(mode, mGroup);
        }
    }


    /**
     * 非透明 背景颜色
     *
     * @param colorRes
     */
    public void setLoadingModeBackgroundColor(@ColorRes int colorRes, ViewGroup group) {
        initLoadingView(group);
        mLoadingView.setModeBackgroundColor(colorRes);
    }

    /**
     * 非透明 背景颜色
     *
     * @param colorString
     */
    public void setLoadingModeBackgroundColor(String colorString, ViewGroup group) {
        initLoadingView(group);
        mLoadingView.setModeBackgroundColor(colorString);
    }

    /**
     * 设置加载图标的样式
     *
     * @param gifDrawarbleResId gif图片的id
     */
    public void setLoadingGifIcon(@IdRes int gifDrawarbleResId, ViewGroup group) {
        initLoadingView(group);
        mLoadingView.setGifIcon(gifDrawarbleResId);
    }

    /**
     * 设置加载页面的背景
     *
     * @param drawarbleResId 背景图片最好是.9的
     */
    public void setLoadingBackground(@DrawableRes int drawarbleResId, ViewGroup group) {
        initLoadingView(group);
        mLoadingView.setLoadingBackground(drawarbleResId);
    }

    /**
     * 设置加载页面的背景
     *
     * @param drawarble 背景图片最好是.9的
     */
    public void setLoadingBackground(Drawable drawarble, ViewGroup group) {
        initLoadingView(group);
        mLoadingView.setLoadingBackground(drawarble);
    }

    /**
     * 设置重试按钮的样式
     *
     * @param styleResId 样式资源id
     */
    public void setLoadingRetryBtnStyle(@DrawableRes int styleResId, ViewGroup group) {
        initLoadingView(group);
        mLoadingView.setRetryBtnStyle(styleResId);
    }

    /**
     * @param mode  样式类型
     * @param group 布局对象
     */
    protected void showLoading(@LoadingView.LoadingMode int mode, ViewGroup group) {
        initLoadingView(group);
        if (mLoadingView != null)
            mLoadingView.showLoading(mode);
    }

    protected void initLoadingView(ViewGroup group) {
        mGroup = group;
        if (mLoadingView == null) {
            mLoadingView = LoadingView.injectViewGroup(group);
        }
    }

    public LoadingView getLodingView(){
        return mLoadingView;
    }

    protected void hideLoading() {
        if (mLoadingView != null)
            mLoadingView.loadingClose();
        mGroup = null;
    }

    protected void onLoadError(String msg, LoadingView.RetryCallBack callBack) {
        if (mLoadingView != null)
            mLoadingView.onError(msg, callBack);
    }


    protected void onLoadError(String msg, LoadingView.RetryCallBack callBack, @DrawableRes int imgid) {
        if (mLoadingView != null){
            mLoadingView.setDateNetStatue(imgid);
            mLoadingView.onError(msg, callBack);
        }

    }

}
