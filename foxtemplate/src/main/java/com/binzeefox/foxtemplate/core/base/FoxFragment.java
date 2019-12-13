package com.binzeefox.foxtemplate.core.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.binzeefox.foxtemplate.core.FoxCore;
import com.binzeefox.foxtemplate.core.base.callbacks.PermissionCallback;
import com.binzeefox.foxtemplate.core.tools.ActivityRequestUtil;
import com.binzeefox.foxtemplate.core.tools.Navigator;
import com.binzeefox.foxtemplate.core.tools.PermissionUtil;
import com.binzeefox.foxtemplate.core.tools.ViewHelper;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 碎片基类
 * @author binze
 * 2019/12/10 13:08
 */
public abstract class FoxFragment extends Fragment {
    private static final String TAG = "FoxFragment";
    private CompositeDisposable dContainer; //RX回收器
    private View root;

    private FoxCore core = FoxCore.get();   //核心

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

    /**
     * 请求权限
     * @author binze 2019/12/10 12:11
     */
    protected void requestPermission(List<String> permissionList
            , final PermissionCallback callback){
        PermissionUtil.get(permissionList).request(this)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        dContainer.add(d);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        callback.callback(aBoolean);
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
     * 生命周期 onCreateView
     * @author binze 2019/12/10 14:00
     */
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = onSetLayoutView(inflater, container);
        dContainer = new CompositeDisposable();
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 清理容器
        if (dContainer != null &&!dContainer.isDisposed()){
            dContainer.dispose();
            dContainer.clear();
        }
        dContainer = null;
    }
}
