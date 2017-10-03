package net.steamcrafted.loadtoast;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Created by Wannes2 on 23/04/2015.
 */
public class LoadToast {

    private String mText = "";
    private LoadToastView mView;
    private ViewGroup mParentView;
    private int mTranslationY = 0;
    private boolean mShowCalled = false;
    private boolean mToastCanceled = false;
    private boolean mInflated = false;
    private boolean mVisible = false;
    private boolean mReAttached = false;

    public LoadToast(Context context){
        mView = new LoadToastView(context);
        mParentView = (ViewGroup) ((Activity) context).getWindow().getDecorView();
    }

    private void cleanup() {
        Log.d("dfsd", "called cleanup " + mParentView.getChildCount());
        int childCount = mParentView.getChildCount();
        for(int i = childCount; i >= 0; i--){
            if(mParentView.getChildAt(i) instanceof LoadToastView){
                View v = mParentView.getChildAt(i);
                ((ViewGroup)v.getParent()).removeView(v);
//                Log.d("sdfsdf", "removed child loadtoast");
            }
        }

        Log.d("dfsd", "after cleanup " + mParentView.getChildCount());

        mInflated = false;
        mToastCanceled = false;
    }

    public LoadToast setTranslationY(int pixels){
        mTranslationY = pixels;
        return this;
    }

    public LoadToast setText(String message){
        mText = message;
        mView.setText(mText);
        return this;
    }

    public LoadToast setTextColor(int color){
        mView.setTextColor(color);
        return this;
    }

    public LoadToast setBackgroundColor(int color){
        mView.setBackgroundColor(color);
        return this;
    }

    public LoadToast setProgressColor(int color){
        mView.setProgressColor(color);
        return this;
    }

    public LoadToast show(){
        mShowCalled = true;
        attach();
        return this;
    }

    private void showInternal(){
        mView.show();
        ViewHelper.setTranslationX(mView, (mParentView.getWidth() - mView.getWidth()) / 2);
        ViewHelper.setAlpha(mView, 0f);
        ViewHelper.setTranslationY(mView, -mView.getHeight() + mTranslationY);
        //mView.setVisibility(View.VISIBLE);
        ViewPropertyAnimator.animate(mView).alpha(1f).translationY(25 + mTranslationY)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(null)
                .setDuration(300).setStartDelay(0).start();

        mVisible = true;
    }

    private void attach() {
        cleanup();

        mReAttached = true;

        mParentView.addView(mView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ViewHelper.setAlpha(mView, 0);
        mParentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewHelper.setTranslationX(mView, (mParentView.getWidth() - mView.getWidth()) / 2);
                ViewHelper.setTranslationY(mView, -mView.getHeight() + mTranslationY);
                mInflated = true;
                if(!mToastCanceled && mShowCalled) showInternal();
            }
        },1);
    }

    public void success(){
        if(!mInflated){
            mToastCanceled = true;
            return;
        }
        if(mReAttached){
            mView.success();
            slideUp();
        }
    }

    public void error(){
        if(!mInflated){
            mToastCanceled = true;
            return;
        }
        if(mReAttached){
            mView.error();
            slideUp();
        }
    }

    private void slideUp(){
        mReAttached = false;

        ViewPropertyAnimator.animate(mView).setStartDelay(1000).alpha(0f)
                .translationY(-mView.getHeight() + mTranslationY)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(!mReAttached){
                            Log.d("animation end","end " + (animation != null));
                            cleanup();
                        }
                    }
                })
                .start();

        mVisible = false;
    }
}
