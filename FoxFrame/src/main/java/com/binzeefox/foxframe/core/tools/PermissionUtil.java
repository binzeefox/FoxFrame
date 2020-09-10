package com.binzeefox.foxframe.core.tools;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.binzeefox.foxframe.core.FoxCore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * 权限管理Util
 * <p>
 * 通过RxJava和Fragment实现
 *
 * @author binze 2019/11/28 11:31
 * update at 2020-09-08 优化大改
 */
public class PermissionUtil {
    private static final String TAG = "PermissionUtil";
    private static final String FRAGMENT_TAG = "request_permission_fragment_tag";   //fragment tag
    private final InnerFragment mFragment;  //主业务Fragment


    /**
     * 私有化构造器
     *
     * @param manager 碎片管理器
     * @author 狐彻 2020/09/08 11:16
     */
    private PermissionUtil(final FragmentManager manager){
        InnerFragment fragment = (InnerFragment) manager.findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            mFragment = fragment;
            return;
        }

        // 若为空，说明第一次添加Fragment
        mFragment = new InnerFragment();
        manager.beginTransaction()
                .add(mFragment, FRAGMENT_TAG)
                .commitNow();
    }


    ///////////////////////////////////////////////////////////////////////////
    // 静态获取
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 通过activity获取
     *
     * @author 狐彻 2020/09/08 11:19
     */
    public static PermissionUtil with(final AppCompatActivity activity){
        return new PermissionUtil(activity.getSupportFragmentManager());
    }

    /**
     * 通过fragment获取
     *
     * @author 狐彻 2020/09/08 11:19
     */
    public static PermissionUtil with(final Fragment fragment) {
        return new PermissionUtil(fragment.getChildFragmentManager());
    }

    ///////////////////////////////////////////////////////////////////////////
    // 参数方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 设置所需检查的权限列表
     *
     * @author 狐彻 2020/09/08 11:24
     */
    public PermissionUtil permissionList(@NonNull List<String> permissionList){
        if (!mFragment._permissionList.isEmpty()) mFragment._permissionList.clear();
        mFragment._permissionList.addAll(permissionList);
        return this;
    }

    /**
     * 仅检查权限
     *
     * @return onNext中返回检查失败的权限列表，若全部通过则返回空列表
     * @author 狐彻 2020/09/08 11:31
     */
    public Observable<List<String>> check(){
        return Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> emitter) throws Exception {
                mFragment._emitter = emitter;
                mFragment.startCheck();
            }
        });
    }

    /**
     * 检查并请求权限
     *
     * @return onNext中返回检查失败的权限列表，若全部通过则返回空列表
     * @author 狐彻 2020/09/08 11:46
     */
    public Observable<List<String>> checkAndRequest(){
        return Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> emitter) throws Exception {
                mFragment._emitter = emitter;
                mFragment.startCheckAndRequest();
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // 内部业务Fragment
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 内部无视图Fragment
     *
     * @author 狐彻 2020/09/08 11:11
     */
    public static class InnerFragment extends Fragment{
        private static final int REQUEST_CODE = 0xff01; //请求码
        private final List<String> _permissionList = new ArrayList<>(); //需要检查的列表
        private ObservableEmitter<List<String>> _emitter;   //Rx发射器

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        /**
         * 检查所有权限并发射结果
         *
         * @author 狐彻 2020/09/08 11:44
         */
        private void startCheck(){
            final List<String> failedList = checkPermissions();
            if (!failedList.isEmpty())
                Log.d(TAG, "startCheck: 权限未通过 => " + failedList);
            _emitter.onNext(failedList);
            _emitter.onComplete();
        }

        /**
         * 检查并请求所有权限，发射结果
         *
         * @author 狐彻 2020/09/08 11:47
         */
        private void startCheckAndRequest(){
            final List<String> failedList = checkPermissions();
            if (!failedList.isEmpty()){
                requestPermissions(failedList.toArray(new String[0]), REQUEST_CODE);
                return;
            }
            _emitter.onNext(failedList);
            _emitter.onComplete();
        }

        /**
         * 检查所有权限
         *
         * @author 狐彻 2020/09/08 11:43
         */
        private List<String> checkPermissions(){
            LogUtil.d(TAG, "checkPermissions: 检查权限 => " + _permissionList);

            final List<String> failedList = new ArrayList<>();
            Context temp = getContext();
            final Context ctx = temp == null ? FoxCore.get().getTopActivity() : temp;

            // 检查所有权限
            _permissionList.forEach(new Consumer<String>() {
                @Override
                public void accept(String permission) {
                    int result = ActivityCompat.checkSelfPermission(ctx, permission);
                    if (result != PERMISSION_GRANTED)
                        failedList.add(permission);
                }
            });

            return failedList;
        }

        /**
         * 权限回调
         *
         * @author 狐彻 2020/09/08 11:50
         */
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode != REQUEST_CODE) return;

            final List<String> failedList = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++){
                if (grantResults[i] != PERMISSION_GRANTED)
                    failedList.add(permissions[i]);
            }

            if (!failedList.isEmpty())
                Log.d(TAG, "startCheckAndRequest: 权限未通过 => " + failedList);
            _emitter.onNext(failedList);
            _emitter.onComplete();
        }
    }
}
