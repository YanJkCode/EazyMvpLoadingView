package com.android.eazymvploadingview;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.eazymvp.base.baseInterface.IBasePresenter;
import com.android.eazymvp.base.baseimpl.view.BaseMvpFragment;

public abstract class BaseLazyLoadingMvpFragment<P extends IBasePresenter> extends BaseMvpFragment<P> {

   protected boolean isGetData;
   private Bundle mOutState;

   @Override
   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      isGetData = true;
      super.onViewCreated(view, mOutState);
   }


   @Override
   public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
      //   进入当前Fragment
      if (enter && !isGetData) {
         isGetData = true;
         //   这里可以做网络请求或者需要的数据刷新操作
         initData();
      } else {
         isGetData = false;
      }
      return super.onCreateAnimation(transit, enter, nextAnim);
   }

   @Override
   public void onSaveInstanceState(@NonNull Bundle outState) {
      super.onSaveInstanceState(outState);
      mOutState = outState;
   }

   @Override
   public void onResume() {
      if (!isGetData) {
         //   这里可以做网络请求或者需要的数据刷新操作
         initData();
         isGetData = true;
      }
      super.onResume();
   }

   @Override
   public void onPause() {
      super.onPause();
      isGetData = false;
   }
}
