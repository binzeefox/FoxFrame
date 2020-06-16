package com.binzeefox.foxframe.core.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;

import com.binzeefox.foxframe.core.FoxCore;
import com.binzeefox.foxframe.core.base.callbacks.PermissionCallback;
import com.binzeefox.foxframe.core.tools.ActivityRequestUtil;
import com.binzeefox.foxframe.core.tools.Navigator;
import com.binzeefox.foxframe.core.tools.PermissionUtil;
import com.binzeefox.foxframe.core.tools.ViewHelper;
import com.binzeefox.foxframe.tools.RxUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Activity基类
 *
 * 分离了视图和onCreate
 * @author binze
 * 2019/12/10 11:48
 */
public abstract class FoxActivity extends AppCompatActivity {
    private static final String TAG = "FoxActivity";
    protected CompositeDisposable dContainer;   //RX回收器

//    private FoxCore core = FoxCore.get();   //获取核心

    private final SparseArray<Disposable> mTimerQueue = new SparseArray<>();
    private final SparseBooleanArray mFlagQueue = new SparseBooleanArray();   //二次点击标识

    /**
     * 通过资源ID加载布局，优先级较低
     * @author binze 2019/12/10 12:07
     */
    protected int onSetLayoutResource() {
        return -1;
    }

    /**
     * 通过View加载布局，优先级较高
     * @author binze 2019/12/10 12:08
     */
    protected View onSetLayoutView() {
        return null;
    }

    /**
     * 代理onCreate 业务部分
     * @author binze 2019/12/10 12:09
     */
    protected abstract void create(Bundle savedInstanceState);

    /**
     * 请求权限
     * @author binze 2019/12/10 12:11
     */
    protected void requestPermission(List<String> permissionList
            , final PermissionCallback callback){
        PermissionUtil.get(permissionList).request(this)
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        dContainer.add(d);
                    }

                    @Override
                    public void onNext(List<String> failedList) {
                        callback.callback(failedList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: 请求权限异常", e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 跳转
     * @author binze 2019/12/10 12:37
     */
    public Navigator navigate(Class<? extends Activity> target){
        return new Navigator(this, target);
    }

    /**
     * 隐式跳转
     * @author binze 2019/12/10 12:54
     */
    public Navigator navigate(Intent intent){
        return new Navigator(this, intent);
    }

    /**
     * 隐式参数跳转
     * @author binze 2019/12/11 11:31
     */
    public Observable<ActivityRequestUtil.Result> requestActivity(Intent intent, Bundle options){
        return ActivityRequestUtil.init(intent, options).request(getSupportFragmentManager());
    }

    /**
     * 从上一界面获取参数
     * @author binze 2019/12/10 12:58
     */
    public Bundle getDataFromNavigate(){
        return Navigator.getDataFromNavigate(getIntent());
    }

    /**
     * 获取视图帮助类
     * @author binze 2019/12/10 13:09
     */
    public ViewHelper getViewHelper(){
        return new ViewHelper() {
            @Override
            public <T extends View> T findView(int id) {
                return findViewById(id);
            }
        };
    }

    /**
     * 判断该方法是否在超时前被调用两次
     *
     * @param timeout   超时时间(ms)
     * @param id    检测id，用于区分事件
     * @return 若调在超时前调用该方法两次，则返回true
     */
    protected boolean checkCallAgain(long timeout, final int id){
        Disposable timer = mTimerQueue.get(id);
        boolean flag = mFlagQueue.get(id);

        if (timer != null && !timer.isDisposed() && flag){
            //在倒计时并且已经点过
            mFlagQueue.delete(id);
            mTimerQueue.delete(id);
            return true;
        }

        if (timer != null) timer.dispose();
        mFlagQueue.put(id, true);

        Observable<Long> ob = Observable.timer(timeout, TimeUnit.MILLISECONDS)
                .compose(RxUtil.<Long>setThreadIO());

        timer = ob.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                mFlagQueue.delete(id);
            }
        });
        mTimerQueue.put(id, timer);
        return false;
    }
    protected boolean checkCallAgain(int timeout){
        return checkCallAgain(timeout, -1); //默认id-1
    }

    /**
     * 生命周期 onCreate
     * @author binze 2019/12/10 13:06
     */
    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (core != null) core.pushActivity(this);
        dContainer = new CompositeDisposable();

        //设置布局
        View layout = onSetLayoutView();
        if (layout != null) setContentView(layout);
        else if (onSetLayoutResource() != -1)
            setContentView(onSetLayoutResource());

        create(savedInstanceState);
    }

    /**
     * 生命周期 onDestroy
     * @author binze 2019/12/10 13:06
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (core != null) core.removeActivity(this);
        if (dContainer != null && !dContainer.isDisposed()){
            dContainer.dispose();
            dContainer.clear();
        }
        dContainer = null;
    }
}
