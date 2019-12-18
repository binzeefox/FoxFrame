package com.binzeefox.foxtemplate.core.tools;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.binzeefox.foxtemplate.R;
import com.binzeefox.foxtemplate.tools.RxUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * 权限管理Util
 * <p>
 * 通过RxJava和Fragment实现
 * @author binze 2019/11/28 11:31
 */
public class PermissionUtil {
    private static final String TAG = "PermissionUtil";
//    private static final int REQUEST_CODE = R.id.code_permission_rx;
    private static final int REQUEST_CODE = 0xff01;
    private static final String REQUEST_TAG = "REQUEST_PERMISSION_FRAGMENT";
    private static PermissionFragment mFragment;
    private List<String> permissionList = new ArrayList<>();

    /**
     * 静态获取
     *
     * @param permissionList 需要申请的权限列表
     * @author binze 2019/11/28 11:46
     */
    public static PermissionUtil get(List<String> permissionList) {
        return new PermissionUtil(permissionList);
    }

    /**
     * 私有化构造器
     *
     * @param permissionList 需要申请的权限列表
     * @author binze 2019/11/28 11:46
     */
    private PermissionUtil(List<String> permissionList) {
        this.permissionList = permissionList;
    }

    /**
     * Activity请求权限
     *
     * @param activity 发起者Activity
     * @author binze 2019/11/29 10:05
     */
    public Observable<Boolean> request(final AppCompatActivity activity) {
        getPermissionFragment(activity.getSupportFragmentManager());
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                mFragment.permissionList = permissionList;
                mFragment.emitter = emitter;
                mFragment.startRequest(activity);
            }
        }).compose(RxUtil.<Boolean>setThreadNew());
    }

    /**
     * Fragment请求权限
     *
     * @param fragment 发起者Fragment
     * @author binze 2019/11/29 10:06
     */
    public Observable<Boolean> request(final Fragment fragment) {
        getPermissionFragment(fragment.getChildFragmentManager());
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                mFragment.permissionList = permissionList;
                mFragment.emitter = emitter;
                mFragment.startRequest(fragment.getActivity());
            }
        }).compose(RxUtil.<Boolean>setThreadNew());
    }

    /**
     * 获取负责权限请求的Fragment
     *
     * @param manager 负责的管理者
     * @author binze 2019/11/29 10:06
     */
    private void getPermissionFragment(final FragmentManager manager) {
        PermissionFragment fragment = (PermissionFragment) manager.findFragmentByTag(REQUEST_TAG);
        if (fragment != null) { //若存在已有的fragment，则返回改fragment
            mFragment = fragment;
            return;
        }

        //若为空则添加入无界面的Fragment
        fragment = new PermissionFragment();
        manager
                .beginTransaction()
                .add(fragment, REQUEST_TAG)
                .commitNow();
        mFragment = fragment;
    }

    /**
     * 负责请求权限的无界面Fragment
     *
     * @author binze 2019/11/28 11:47
     */
    public static class PermissionFragment extends Fragment {
        private ObservableEmitter<Boolean> emitter; //RxJava的发射器
        private List<String> permissionList = new ArrayList<>();    //权限列表

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        /**
         * 开始请求
         *
         * @param activity 请求的宿主activity
         * @author binze 2019/11/29 10:09
         */
        private void startRequest(Activity activity) {
            List<String> failedList = new ArrayList<>();
            for (String permission : permissionList) {
                int flag = ActivityCompat.checkSelfPermission(activity, permission);
                if (flag != PackageManager.PERMISSION_GRANTED) failedList.add(permission);
            }
            if (failedList.isEmpty()) {
                emitter.onNext(true);
                emitter.onComplete();
            } else {
                requestPermissions(failedList.toArray(new String[0]), REQUEST_CODE);
            }
        }

        /**
         * 权限回调
         *
         * @author binze 2019/11/29 10:09
         */
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode != REQUEST_CODE) return;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    emitter.onNext(false);
                    emitter.onComplete();
                    return;
                }
            }
            emitter.onNext(true);
            emitter.onComplete();
        }
    }
}
