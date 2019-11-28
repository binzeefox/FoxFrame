package com.binzeefox.foxtemplate.utils;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.binzeefox.foxtemplate.base.FoxActivity;
import com.binzeefox.foxtemplate.base.FoxApplication;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * 权限管理Util
 *
 * TODO 待验证
 * @author binze 2019/11/28 11:31
 */
public class PermissionUtil {
    private static final String TAG = "PermissionUtil";
    private static final int REQUEST_CODE = 0x025;
    private static FoxApplication mApp = FoxApplication.get();
    private List<String> permissionList = new ArrayList<>();

    /**
     * 静态获取
     *
     * @param permissionList 请求的权限列表
     * @author binze 2019/11/28 11:46
     */
    public static PermissionUtil get(List<String> permissionList) {
        return new PermissionUtil(permissionList);
    }

    /**
     * 私有化构造器
     *
     * @author binze 2019/11/28 11:46
     */
    private PermissionUtil(List<String> permissionList) {
        this.permissionList = permissionList;
    }

    public Observable<Boolean> request(){
        FoxActivity activity = mApp.getTopActivity();

        return Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            Fragment requestFragment = new PermissionFragment(emitter, ft);
            ft.add(requestFragment, "REQUEST_PERMISSION");
        }).compose(RxUtil.setThread());
    }

    /**
     * 负责请求权限的Fragment
     *
     * @author binze 2019/11/28 11:47
     */
    private class PermissionFragment extends Fragment {
        private final FragmentTransaction ft;
        private ObservableEmitter<Boolean> emitter;

        private PermissionFragment(ObservableEmitter<Boolean> emitter, FragmentTransaction ft){
            this.emitter = emitter;
            this.ft = ft;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            FoxActivity activity = mApp.getTopActivity();

            List<String> failedList = new ArrayList<>();
            for (String permission : permissionList){
                int flag = ActivityCompat.checkSelfPermission(activity, permission);
                if (flag != PackageManager.PERMISSION_GRANTED) failedList.add(permission);
            }
            if (failedList.isEmpty()) {
                emitter.onNext(true);
                emitter.onComplete();
            } else {
                ActivityCompat.requestPermissions
                        (activity, failedList.toArray(new String[0]), REQUEST_CODE);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode != REQUEST_CODE) return;
            for (int result : grantResults){
                if (result != PackageManager.PERMISSION_GRANTED){
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
