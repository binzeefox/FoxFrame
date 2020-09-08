package com.binzeefox.foxframe.core.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.binzeefox.foxframe.core.FoxCore;
import com.binzeefox.foxframe.core.base.callbacks.PermissionCallback;
import com.binzeefox.foxframe.core.tools.ActivityRequestUtil;
import com.binzeefox.foxframe.core.tools.Navigator;
import com.binzeefox.foxframe.core.tools.PermissionUtil;
import com.binzeefox.foxframe.core.tools.ViewHelper;
import com.binzeefox.foxframe.tools.RxUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 碎片基类
 * @author binze
 * 2019/12/10 13:08
 */
public abstract class FoxFragment extends Fragment {
    private static final String TAG = "FoxFragment";
    protected CompositeDisposable dContainer; //RX回收器
    protected io.reactivex.rxjava3.disposables.CompositeDisposable dContainer3; //RX回收器
    private View root;

    private FoxCore core = FoxCore.get();   //核心
    private SparseArray<Disposable> mTimerQueue = new SparseArray<>();
    private SparseBooleanArray mFlagQueue = new SparseBooleanArray();   //二次点击标识

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
    protected View onSetLayoutView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return null;
    }

    /**
     * 代理onCreateView 业务部分
     * @author binze 2019/12/10 13:18
     */
    protected abstract void create(View root, Bundle savedInstanceState);

//    /**
//     * 请求权限
//     * @author binze 2019/12/10 12:11
//     */
//    protected void requestPermission(List<String> permissionList
//            , final PermissionCallback callback){
//        PermissionUtil.get(permissionList).request(this)
//                .subscribe(new Observer<List<String>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        dContainer.add(d);
//                    }
//
//                    @Override
//                    public void onNext(List<String> failedList) {
//                        callback.callback(failedList);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e(TAG, "onError: 请求权限异常", e);
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//    }

    /**
     * 跳转
     * @author binze 2019/12/10 12:37
     */
    public Navigator navigate(Class<? extends Activity> target){
        return new Navigator(this, target);
    }

    /**
     * 隐式跳转
     * @author binze 2019/12/10 13:56
     */
    public Navigator navigator(Intent intent){
        return new Navigator(this, intent);
    }

    /**
     * 隐式参数跳转
     * @author binze 2019/12/11 11:31
     */
    public Observable<ActivityRequestUtil.Result> requestActivity(Intent intent, Bundle options){
        return ActivityRequestUtil.init(intent, options).request(getChildFragmentManager());
    }

    /**
     * 获取视图帮助类
     * @author binze 2019/12/10 13:59
     */
    public ViewHelper getViewHelper(){
        return new ViewHelper() {
            @Override
            public <T extends View> T findView(int id) {
                return root.findViewById(id);
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
     * 生命周期 onCreateView
     * @author binze 2019/12/10 14:00
     */
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = onSetLayoutView(inflater, container);
        dContainer = new CompositeDisposable();
        dContainer3 = new io.reactivex.rxjava3.disposables.CompositeDisposable();
        if (root == null && onSetLayoutResource() != -1)
            root = inflater.inflate(onSetLayoutResource(), container, false);
        create(root, savedInstanceState);
        return root;
    }

    /**
     * 生命周期 onResume
     * @author binze 2019/12/10 14:05
     */
    @Override
    public void onResume() {
        super.onResume();
        if (dContainer == null || dContainer.isDisposed())
            dContainer = new CompositeDisposable();
        if (dContainer3 == null || dContainer3.isDisposed())
            dContainer3 = new io.reactivex.rxjava3.disposables.CompositeDisposable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 清理容器
        if (dContainer != null &&!dContainer.isDisposed()){
            dContainer.dispose();
            dContainer.clear();
        }
        if (dContainer3 != null &&!dContainer3.isDisposed()){
            dContainer3.dispose();
            dContainer3.clear();
        }
        dContainer = null;
        dContainer3 = null;
    }
}
