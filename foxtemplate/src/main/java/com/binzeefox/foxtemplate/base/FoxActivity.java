package com.binzeefox.foxtemplate.base;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.binzeefox.foxtemplate.R;
import com.binzeefox.foxtemplate.base.interfaces.FoxContext;
import com.binzeefox.foxtemplate.base.interfaces.ViewHelper;
import com.binzeefox.foxtemplate.utils.StringChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import io.reactivex.disposables.CompositeDisposable;

/**
 * 活动基类。
 * <p>
 * 封装onCreate和动态权限获取
 * 封装了一个弹窗Fragment
 * 封装跳转
 */
@SuppressWarnings("SameParameterValue")
public abstract class FoxActivity extends AppCompatActivity implements FoxContext {
    private static final String TAG = "FoxActivity";
    private static final int PERMISSION_CODE = R.id.code_permission;    //权限请求码
    protected CompositeDisposable dContainer;   //RX回收器

//    ******↓生命周期

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFoxApplication().registerActivity(this);
        dContainer = new CompositeDisposable();

        // @author binze
        // update in 2019/12/2 14:58
        if (onInflateLayout() != -1)    //若重写了资源文件方法，则添加资源文件Layout
            setContentView(onInflateLayout());
        else if (onSetLayout() != null) //若重写了View方法， 则添加View
            setContentView(onSetLayout());
        create(savedInstanceState);

        //Check and request permission
        List<String> permissionList = getPermissionList(onCheckPermission());
        if (permissionList != null && !permissionList.isEmpty())
            requestSelfPermissions(checkSelfPermissions(permissionList));
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
     * @param bundle     返回数据
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
     * @return 布局资源id，将会用于{@link #setContentView(int)} 方法的参数
     * update in 2019-12-02 14:50
     * 改为继承方法，同时添加{@link #onSetLayout()} 方法，继承时可选择直接添加View还是添加资源id
     */
    protected int onInflateLayout() {
        return -1;
    }

    /**
     * 加载布局
     *
     * @return 布局，将会用于{@link #setContentView(View)} 方法的参数，优先级低于{@link #onInflateLayout()}
     * @author binze 2019/12/2 14:54
     */
    protected View onSetLayout() {
        return null;
    }

    /**
     * 代理onCreate
     * <p>
     * 承担了原来{@link #onCreate(Bundle)}的业务部分，隐藏了布局部分
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
     * 获取需要检查的权限，在Activity创建时进行检查。若需要指定时机检查，可以无视此方法
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
//        if (failedList.size() == 0)
//            return;
//        StringBuilder sb = new StringBuilder();
//        for (String permission : failedList) {
//            if (!sb.toString().isEmpty())
//                sb.append("和");
//            sb.append(permission).append("\n");
//        }
//        sb.append("获取失败");
//
//        CustomDialogFragment.get(this)
//                .cancelable(true)
//                .title("权限获取失败")
//                .message(sb.toString())
//                .show(getSupportFragmentManager());
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
    protected Bundle getDataFromIntent(Intent intent) {
        Bundle bundle;

        if (intent == null) return new Bundle();
        bundle = intent.getBundleExtra("params");
        if (bundle == null) return new Bundle();
        return bundle;
    }

    /**
     * 获取返回值
     *
     * @param requestCode 请求码
     * @param resultCode  返回码
     * @param params      返回值
     */
    protected void onResultReturn(int requestCode, int resultCode, @Nullable Bundle params) {
    }

    /**
     * 接口空实现，用于指定intent跳转
     *
     * @param intent      跳转intent
     * @param requestCode 请求码
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> failedList = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++)
            if (PackageManager.PERMISSION_GRANTED != grantResults[i])
                failedList.add(permissions[i]);
        onSelfPermissionResult(failedList);
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (failedList.isEmpty()) return;
        for (Fragment fragment : fragmentList)
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

//    特殊方法 ↓

    /**
     * 获取视图帮助类，用于快捷的对视图进行操作
     * @author binze 2019/12/3 15:14
     */
    protected ViewHelper getViewHelper(){
        return new ViewHelper() {
            @Override
            public String getStringById(int id) {
                View view = findViewById(id);
                if (!(view instanceof TextView)) return null;
                return ((TextView) view).getText().toString();
            }

            @Override
            public double getDoubleById(int id) {
                View view = findViewById(id);
                if (!(view instanceof TextView))
                    throw new IllegalArgumentException("参数应为TextView或其子类!!");
                String str = ((TextView) view).getText().toString();
                try {
                    return Double.parseDouble(str);
                }catch (Exception e){
                    throw new IllegalStateException("TextView的值非纯数字", e);
                }
            }

            @Override
            public int getIntegerById(int id) {
                View view = findViewById(id);
                if (!(view instanceof TextView))
                    throw new IllegalArgumentException("参数应为TextView或其子类!!");
                String str = ((TextView) view).getText().toString();
                try {
                    return Integer.parseInt(str);
                }catch (Exception e){
                    throw new IllegalStateException("TextView的值非纯数字", e);
                }
            }

            @Override
            public void setErrorById(int id, CharSequence error) {
                View view = findViewById(id);
                if (!(view instanceof EditText)){
                    Log.w(TAG, "setErrorById: 目标View非EditText子类");
                    return;
                }
                ((EditText) view).setError(error);
            }

            @Override
            public void setErrorById(int id, int strId) {
                View view = findViewById(id);
                if (!(view instanceof EditText)){
                    Log.w(TAG, "setErrorById: 目标View非EditText子类");
                    return;
                }
                ((EditText) view).setError(getString(strId));
            }

            @Override
            public void setTextById(int id, CharSequence text) {
                View view = findViewById(id);
                if (!(view instanceof TextView)){
                    Log.w(TAG, "setErrorById: 目标View非TextView子类");
                    return;
                }
                ((TextView) view).setText(text);
            }

            @Override
            public void setTextById(int id, int strId) {
                View view = findViewById(id);
                if (!(view instanceof TextView)){
                    Log.w(TAG, "setErrorById: 目标View非TextView子类");
                    return;
                }
                ((TextView) view).setText(strId);
            }

            @Override
            public void clearViewById(int id) {
                View view = findViewById(id);
                if (!(view instanceof TextView)){
                    Log.w(TAG, "setErrorById: 目标View非TextView子类");
                    return;
                }
                ((TextView) view).setError(null);
                ((TextView) view).setText(null);
            }

            @Override
            public boolean checkVisibility(View view) {
                return view.getVisibility() == View.VISIBLE;
            }

            @Override
            public boolean checkVisibilityById(int id) {
                return findViewById(id).getVisibility() == View.VISIBLE;
            }

            @Override
            public boolean checkFieldEmptyById(int id) {
                View view = findViewById(id);
                if (!(view instanceof TextView)){
                    Log.w(TAG, "setErrorById: 目标View非TextView子类");
                    return true;
                }
                return TextUtils.isEmpty(((TextView) view).getText());
            }

            @Override
            public boolean checkEnable(View view) {
                return view.isEnabled();
            }

            @Override
            public boolean checkEnableById(int id) {
                return findViewById(id).isEnabled();
            }

            @Override
            public void setViewsVisibility(int visibility, View... views) {
                for (View view : views) view.setVisibility(visibility);
            }

            @Override
            public void setViewsVisibilityByIds(int visibility, int... ids) {
                for (int id : ids) findViewById(id).setVisibility(visibility);
            }

            @Override
            public void setViewsEnable(boolean enable, View... views) {
                for (View view : views) view.setEnabled(enable);
            }

            @Override
            public void setViewsEnableById(boolean enable, int... ids) {
                for (int id : ids) findViewById(id).setEnabled(enable);
            }
        };
    }
}
