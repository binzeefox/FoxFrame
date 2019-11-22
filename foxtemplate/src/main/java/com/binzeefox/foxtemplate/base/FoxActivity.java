package com.binzeefox.foxtemplate.base;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.binzeefox.foxtemplate.R;
import com.binzeefox.foxtemplate.customviews.CustomDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
//import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

/**
 * 活动基类。
 * <p>
 * 封装onCreate和动态权限获取
 * 封装了一个弹窗Fragment
 * 封装跳转
 */
@SuppressWarnings("SameParameterValue")
public abstract class FoxActivity extends AppCompatActivity implements FoxContext{
    private static final int PERMISSION_CODE = R.id.code_permission;    //权限请求码
    protected CompositeDisposable dContainer;   //RX回收器

//    ******↓生命周期

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFoxApplication().registerActivity(this);
        dContainer = new CompositeDisposable();
        setContentView(onInflateLayout());
        //ButterKnife Binder here...
//        ButterKnife.bind(this);

        create(savedInstanceState);
        //Check and request permission
        List<String> permissionList = getPermissionList(onCheckPermission());
        if (permissionList != null && !permissionList.isEmpty())
            requestSelfPermissions(checkSelfPermissions(permissionList));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        setIntent(intent);    //若不走该方法，则getIntent获取到的是旧的intent值
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getFoxApplication().unRegisterActivity(this);
        if (dContainer != null && !dContainer.isDisposed()) {//rx_java注意isDisposed是返回是否取消订阅
            dContainer.dispose();
            dContainer.clear();
            dContainer = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle params = null;
        if (data != null)
            params = data.getBundleExtra("params");
        onResultReturn(requestCode, resultCode, params);
    }

    //    ******↓公共方法

    /**
     * 无参无返回跳转
     *
     * @param target 目标Activity
     */
    public void navigate(Class<? extends Activity> target) {
        navigate(target, null);
    }

    /**
     * 无返回跳转
     *
     * @param params 参数
     * @param target 目标Activity
     */
    @Override
    public void navigate(Class<? extends Activity> target, @Nullable Bundle params) {
        Intent intent = new Intent(this, target);
        if (params != null)
            intent.putExtra("params", params);
        startActivity(intent);
    }

    /**
     * 返回值跳转
     *
     * @param target      目标Activity
     * @param requestCode 请求码
     */
    public void navigateForResult(Class<? extends Activity> target, int requestCode) {
        navigateForResult(target, null, requestCode);
    }

    /**
     * 返回值跳转
     *
     * @param target      目标Activity
     * @param params      参数
     * @param requestCode 请求码
     */
    @Override
    public void navigateForResult(Class<? extends Activity> target, Bundle params, int requestCode) {
        Intent intent = new Intent(this, target);
        if (params != null)
            intent.putExtra("params", params);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 设置返回值
     *
     * @param bundle      返回数据
     * @param resultCode 返回码
     */
    protected void setResultAndReturn(Bundle bundle, int resultCode) {
        Intent intent = new Intent();
        intent.putExtra("params", bundle);
        setResult(resultCode, intent);
        finish();
    }


//    ******↓继承方法

    /**
     * 加载布局
     *
     * @return 布局资源id
     */
    protected abstract int onInflateLayout();

    /**
     * 代理onCreate
     */
    protected abstract void create(Bundle savedInstanceState);

    /**
     * Application类型转换
     *
     * @return 定制的Application
     */
    protected FoxApplication getFoxApplication() {
        return (FoxApplication) super.getApplication();
    }

    /**
     * 获取需要检查的权限
     *
     * @return 权限数组
     */
    protected String[] onCheckPermission() {
        return null;
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

        CustomDialogFragment.get(this)
                .cancelable(true)
                .title("权限获取失败")
                .message(sb.toString())
                .show(getSupportFragmentManager());
    }

    /**
     * 获取上一界面传来的信息
     */
    protected Bundle getDataFromNavigate() {
        Bundle bundle;

        Intent incomeIntent = getIntent();
        if (incomeIntent == null) return new Bundle();
        bundle = incomeIntent.getBundleExtra("params");
        if (bundle == null) return new Bundle();
        return bundle;
    }

    /**
     * 从intent中获取信息
     *
     * @param intent 来源intent
     * @author binze 2019/11/1 9:23
     */
    protected Bundle getDataFromIntent(Intent intent){
        Bundle bundle;

        if (intent == null) return new Bundle();
        bundle = intent.getBundleExtra("params");
        if (bundle == null) return new Bundle();
        return bundle;
    }

    /**
     * 获取返回值
     * @param requestCode   请求码
     * @param resultCode    返回码
     * @param params    返回值
     */
    protected void onResultReturn(int requestCode, int resultCode, @Nullable Bundle params){}

    /**
     * 接口空实现，用于指定intent跳转
     * @param intent    跳转intent
     * @param requestCode   请求码
     */
    @Override
    public void navigateForResult(Intent intent, int requestCode) {

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
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission))
                failedList.add(permission);
        return failedList;
    }

    /**
     * 请求动态权限
     */
    @RequiresApi(Build.VERSION_CODES.M)
    protected void requestSelfPermissions(List<String> permissions) {
        if (permissions == null || permissions.isEmpty())
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
