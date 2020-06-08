package com.binzeefox.foxframe.core.tools;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.binzeefox.foxframe.tools.RxUtil;

import java.util.List;

import androidx.annotation.IdRes;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static android.content.ContentValues.TAG;

/**
 * 视图工具类
 * @author binze
 * 2019/12/10 12:16
 */
public abstract class ViewHelper {
    
    /**
     * 获取目标
     * @author binze 2019/12/10 12:18
     */
    public abstract <T extends View> T findView(int id);

    /**
     * 通过id获取文字
     * @author binze 2019/12/10 12:21
     */
    public String getStringById(int id) {
        View view = findView(id);
        if (!(view instanceof TextView)) return null;
        return ((TextView) view).getText().toString();
    }

    /**
     * 通过id获取数字
     * @author binze 2019/12/10 12:22
     */
    public double getDoubleById(int id) {
        View view = findView(id);
        if (!(view instanceof TextView))
            throw new RuntimeException(new IllegalArgumentException("参数应为TextView或其子类!!"));
        String str = ((TextView) view).getText().toString();
        try {
            return Double.parseDouble(str);
        }catch (Exception e){
            throw new IllegalStateException("TextView的值非纯数字", e);
        }
    }

    /**
     * 通过id获取数字
     * @author binze 2019/12/10 12:22
     */
    public int getIntegerById(int id) {
        View view = findView(id);
        if (!(view instanceof TextView))
            throw new IllegalArgumentException("参数应为TextView或其子类!!");
        String str = ((TextView) view).getText().toString();
        try {
            return Integer.parseInt(str);
        }catch (Exception e){
            throw new IllegalStateException("TextView的值非纯数字", e);
        }
    }

    /**
     * 通过id设置异常状态
     * @author binze 2019/12/10 12:23
     */
    public void setErrorById(int id, CharSequence error) {
        View view = findView(id);
        if (!(view instanceof EditText)){
            Log.w(TAG, "setErrorById: 目标View非EditText子类");
            return;
        }
        ((EditText) view).setError(error);
    }

    /**
     * 通过id设置文字
     * @author binze 2019/12/10 12:24
     */
    public void setTextById(int id, CharSequence text) {
        View view = findView(id);
        if (!(view instanceof TextView)){
            Log.w(TAG, "setErrorById: 目标View非TextView子类");
            return;
        }
        ((EditText) view).setText(text);
    }

    /**
     * 清空View状态
     * @author binze 2019/12/10 12:24
     */
    public void clearViewById(int id) {
        View view = findView(id);
        if (!(view instanceof EditText)){
            Log.w(TAG, "setErrorById: 目标View非TextView子类");
            return;
        }
        ((EditText) view).setError(null);
        ((EditText) view).setText(null);
    }

    /**
     * 检查是否可见
     * @author binze 2019/12/10 12:24
     */
    public boolean checkVisibility(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    /**
     * 检查是否可见
     * @author binze 2019/12/10 12:25
     */
    public boolean checkVisibilityById(int id) {
        return findView(id).getVisibility() == View.VISIBLE;
    }

    /**
     * 检查可用状态
     * @author binze 2019/12/10 12:26
     */
    public boolean checkEnable(View view) {
        return view.isEnabled();
    }

    /**
     * 检查可用状态
     * @author binze 2019/12/10 12:26
     */
    public boolean checkEnableById(int id) {
        return findView(id).isEnabled();
    }

    /**
     * 批量设置可见性
     * @author binze 2019/12/10 12:27
     */
    public void setViewsVisibility(int visibility, View... views) {
        for (View view : views) view.setVisibility(visibility);
    }

    /**
     * 批量设置可见性
     * @author binze 2019/12/10 12:27
     */
    public void setViewsVisibilityByIds(int visibility, int... ids) {
        for (int id : ids) findView(id).setVisibility(visibility);
    }

    /**
     * 批量设置可用状态
     * @author binze 2019/12/10 12:27
     */
    public void setViewsEnable(boolean enable, View... views) {
        for (View view : views) view.setEnabled(enable);
    }

    /**
     * 批量设置可用状态
     * @author binze 2019/12/10 12:27
     */
    public void setViewsEnableById(boolean enable, int... ids) {
        for (int id : ids) findView(id).setEnabled(enable);
    }

    /**
     * 同步加载视图
     * @author binze 2020/4/24 10:01
     */
    public Observable<View> loadViewAsync(@IdRes final int id){
        return Observable.create(new ObservableOnSubscribe<View>() {
            @Override
            public void subscribe(ObservableEmitter<View> e) throws Exception {
                View view = findView(id);
                e.onNext(view);
                e.onComplete();
            }
        }).compose(RxUtil.<View>setThreadIO());
    }

    /**
     * 同步加载多视图
     * @author binze 2020/4/24 10:22
     */
    public Observable<View> loadViewsAsync(@IdRes final int... ids){
        return Observable.create(new ObservableOnSubscribe<View>() {
            @Override
            public void subscribe(ObservableEmitter<View> e) throws Exception {
                for (int id : ids)
                    e.onNext(findView(id));
                e.onComplete();
            }
        }).compose(RxUtil.<View>setThreadIO());
    }

    /**
     * 同步加载多视图
     * @author binze 2020/4/24 10:22
     */
    public Observable<View> loadViewsAsync(final List<Integer> ids){
        return Observable.create(new ObservableOnSubscribe<View>() {
            @Override
            public void subscribe(ObservableEmitter<View> e) throws Exception {
                for (int id : ids)
                    e.onNext(findView(id));
                e.onComplete();
            }
        }).compose(RxUtil.<View>setThreadIO());
    }
}
