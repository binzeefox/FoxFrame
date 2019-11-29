package com.binzeefox.foxtemplate.base;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import io.reactivex.disposables.CompositeDisposable;

/**
 * 碎片基类
 * <p>
 * 封装了一个弹窗Fragment
 * 封装跳转
 */
public abstract class FoxFragment extends Fragment implements FoxContext{
    private static final String TAG = "FoxFragment";
    private static final int PERMISSION_CODE = 0x99;

    private CompositeDisposable dContainer;   //RX回收器


    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(inflateLayout(), container, false);
        dContainer = new CompositeDisposable();
        // 接管onCreateView
        create(view, savedInstanceState);
        return view;
    }

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
        if (!dContainer.isDisposed())
            dContainer.dispose();
    }

//    ****抽象方法↓****

    /**
     * 接管 onCreateView
     */
    protected abstract void create(@NonNull View view, @Nullable Bundle savedInstanceState);

    /**
     * 加载布局
     * @return  布局id
     */
    protected abstract int inflateLayout();


//    ****继承方法↓****

    /**
     * 无参无返回跳转
     * @param target 目标Activity
     */
    @Override
    public void navigate(Class<? extends Activity> target) {
        navigate(target, null);
    }

    /**
     * 无返回跳转
     * @param params 参数
     * @param target 目标Activity
     */
    @Override
    public void navigate(Class<? extends Activity> target, Bundle params) {
        Intent intent = new Intent(getActivity(), target);
        if (params != null)
            intent.putExtra("params", params);
        startActivity(intent);
    }

    /**
     * 返回值跳转
     * @param target 目标Activity
     * @param requestCode   请求码
     */
    @Override
    public void navigateForResult(Class<? extends Activity> target, int requestCode){
        navigateForResult(target, null, requestCode);
    }

    /**
     * 返回值跳转
     * @param target 目标Activity
     * @param params 参数
     * @param requestCode   请求码
     */
    @Override
    public void navigateForResult(Class<? extends Activity> target, Bundle params, int requestCode){
        Intent intent = new Intent(getActivity(), target);
        if (params != null)
            intent.putExtra("params", params);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 权限请求结果
     *
     * @param failedList 尚未通过的权限
     */
    protected void onSelfPermissionResult(List<String> failedList) {
        if (failedList.size() == 0)
            return;
        StringBuilder sb = new StringBuilder();
        for (String permission : failedList) {
            if (!sb.toString().isEmpty())
                sb.append("和");
            sb.append(permission).append("\n");
        }
        sb.append("获取失败");

        Log.w(TAG, "onSelfPermissionResult: failed for ->" + sb);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    //    ******↓权限相关

    /**
     * 检查动态权限
     *
     * @return 尚未通过的权限
     */
    @RequiresApi(Build.VERSION_CODES.M)
    protected List<String> checkSelfPermissions(List<String> permissions) {
        List<String> failedList = new ArrayList<>();
        for (String permission : permissions)
            if (PackageManager.PERMISSION_GRANTED
                    != ActivityCompat.checkSelfPermission(FoxApplication.get(), permission))
                failedList.add(permission);
        return failedList;
    }

    /**
     * 请求动态权限
     */
    @RequiresApi(Build.VERSION_CODES.M)
    protected void requestSelfPermissions(List<String> permissions) {
        if (permissions == null)
            return;
        String[] requests = new String[permissions.size()];
        for (int i = 0; i < permissions.size(); i++)
            requests[i] = permissions.get(i);
        requestPermissions(requests, PERMISSION_CODE);
    }

    /**
     * 权限格式转换
     */
    private List<String> getPermissionList(String[] permissions) {
        if (permissions == null)
            return null;
        return Arrays.asList(permissions);
    }

    /**
     * 权限回调
     */
    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        List<String> failedList = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++)
            if (PackageManager.PERMISSION_GRANTED != grantResults[i])
                failedList.add(permissions[i]);
        onSelfPermissionResult(failedList);
    }
}
