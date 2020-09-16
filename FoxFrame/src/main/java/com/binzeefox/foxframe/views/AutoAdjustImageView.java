package com.binzeefox.foxframe.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.binzeefox.foxframe.tools.dev.LogUtil;

/**
 * 高度自适应图片视图
 * @author binze
 * 2019/11/19 9:46
 */
public class AutoAdjustImageView extends AppCompatImageView {
    private static final String TAG = "AutoAdjustImageView";

    // 尺寸改变回调
    private OnSizeChangedListener mListener = new OnSizeChangedListener() {
        @Override
        public void onSizeChange(int width, int height) {
        }
    };

    public AutoAdjustImageView(Context context) {
        super(context);
    }

    public AutoAdjustImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoAdjustImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Drawable d = getDrawable(); //先获取到图片
        if (d == null) {    //没有图片则结束
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        //视图尺寸
        int width = MeasureSpec.getSize(widthMeasureSpec),  height = MeasureSpec.getSize(heightMeasureSpec);
        int[] measures;
        LogUtil.d(TAG, "onMeasure: " + width + " " + height);

        //若长非确认值，则直接宽自适应
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST){
            measures = fitWidth(widthMeasureSpec);
            width = measures[0];
            height = measures[1];
            mListener.onSizeChange(width, height);
            setMeasuredDimension(width, height);
            Log.d(TAG, "onMeasure: end1 " + width + " " + height);

            return;
        }

        //先宽自适应，若长度超标，则改为长自适应
        //1.宽度自适应
        measures = fitWidth(widthMeasureSpec);
        width = measures[0];
        height = measures[1];

        //2.若长度超标，则改为长自适应
        Log.d(TAG, "onMeasure: height heightRaw " + height + " " + MeasureSpec.getSize(heightMeasureSpec));
        if (height > MeasureSpec.getSize(heightMeasureSpec)){
            measures = fitHeight(heightMeasureSpec);
            width = measures[0];
            height = measures[1];
        }

        setMeasuredDimension(width, height);
        mListener.onSizeChange(width, height);
        LogUtil.d(TAG, "onMeasure: end " + width + " " + height);
    }

    /**
     * 设置尺寸变化监听
     * @author binze 2019/11/19 9:47
     */
    public void setOnSizeChangedListener(OnSizeChangedListener listener){
        mListener = listener;
    }

    /**
     * 长度自适应
     */
    private int[] fitWidth(int widthMeasureSpec){
        int width = 0,  height = 0;
        Drawable d = getDrawable();

        width = MeasureSpec.getSize(widthMeasureSpec);
        height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());

        return new int[]{width, height};
    }

    /**
     * 宽度自适应
     */
    private int[] fitHeight(int heightMeasureSpec){
        int width = 0,  height = 0;
        Drawable d = getDrawable();

        height = MeasureSpec.getSize(heightMeasureSpec);
        Log.d(TAG, "fitHeight: " + height);
        width = (int) Math.ceil((float) height * (float) d.getIntrinsicWidth() / (float) d.getIntrinsicHeight());

        return new int[]{width, height};
    }

    /**
     * 尺寸变化回调
     *
     * @author 狐彻 2020/09/10 14:09
     */
    public interface OnSizeChangedListener{
        void onSizeChange(int width, int height);
    }
}
