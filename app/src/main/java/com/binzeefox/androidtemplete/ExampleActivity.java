package com.binzeefox.androidtemplete;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.binzeefox.foxframe.core.FoxCore;
import com.binzeefox.foxframe.core.base.FoxActivity;
import com.binzeefox.foxframe.core.base.callbacks.PermissionCallback;
import com.binzeefox.foxframe.core.tools.PermissionUtil;
import com.binzeefox.foxframe.tools.dev.TextTools;
import com.binzeefox.foxframe.tools.image.ImageUtil;
import com.binzeefox.foxframe.tools.image.RxImagePicker;
import com.binzeefox.foxframe.tools.phone.NoticeUtil;
import com.binzeefox.foxframe.tools.phone.PhoneStatusUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.annotation.RequiresPermission;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 示例活动
 * @author binze
 * 2020/1/7 14:03
 */
public class ExampleActivity extends FoxActivity {
    private static final String TAG = "ExampleActivity";
    private Button button;
    private EditText editText;

    // 通过ID加载布局
    @Override
    protected int onSetLayoutResource() {
        return R.layout.activity_main;
    }

    // 通过View加载布局, 优先级高于ID加载
    @Override
    protected View onSetLayoutView() {
        return getLayoutInflater().inflate(R.layout.activity_main, null);
    }

    // 业务承载类
    @Override
    protected void create(Bundle savedInstanceState) {
        Context context = FoxCore.getApplication(); //若配置了FoxCore，则该方法获得的是Application实例
        // 接管onCreate
        button = findViewById(R.id.btn_btn);
//        button.setOnClickListener(v -> checkPermissionExample());
        editText = findViewById(R.id.edit_field);
    }

    /**
     * 常用控件操作展示
     * 详见 {@link com.binzeefox.foxframe.core.tools.ViewHelper}
     */
    private void fieldExample(){
        // 设置可用性
        getViewHelper().setViewsEnableById(true, R.id.bottom, R.id.edit_field);
        getViewHelper().setViewsEnable(true, button, editText);

        // 设置可见性
        getViewHelper().setViewsVisibilityByIds(View.VISIBLE, R.id.bottom, R.id.edit_field);
        getViewHelper().setViewsVisibility(View.VISIBLE, button, editText);

        // 设置错误状态
        getViewHelper().setErrorById(R.id.edit_field, "输入错误");

        // 设置文字
        getViewHelper().setTextById(R.id.edit_field, "设置的文字");
    }

    /**
     * 跳转方式
     */
    private void navigateExample(){
        Intent intent;  //跳转intent
        intent = new Intent(this, SecondActivity.class);
        Bundle bundle;  //跳转参数
        bundle = new Bundle();

        // 显式跳转则直接修改intent相关内容
        navigate(intent)
                .setParams(bundle)  //若有参数，则添加参数
                .configIntent(intent1 -> {  //在跳转前对intent进行设置
                    intent.putExtra("some extra", "extra value");
                })
//                .commitForResult(0x01)    //若是请求跳转，则将下面的方法换成该方法
                .commit();  //最终跳转的方法。

        // 若需要上一页面跳转得到的数据，可以用下面的方法
        Bundle income = getDataFromNavigate();
    }

    /**
     * 直接使用基类封装的方法请求权限
     */
    public void checkPermissionExample(){
        requestPermission(Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION)
                , failedList -> {
                    //若全部成功，则返回空列表，否则返回失败的权限
                    Log.d(TAG, "onNext: " + failedList.toString());
                });
    }

    /**
     * 请求权限，灵感来自于RxPermission
     */
    public void checkRxPermissionExample(){
        PermissionUtil.get(Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION))
                .request(this).subscribe(new Observer<List<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                dContainer.add(d);  //将回收器放入容器。每个Activity和Fragment都有一个该容器。在Destroy时进行回收
            }

            @Override
            public void onNext(List<String> failedList) {
                //若全部成功，则返回空列表，否则返回失败的权限
                Log.d(TAG, "onNext: " + failedList.toString());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 利用 {@link FoxActivity#checkCallAgain(int)}   实现二次返回键退出功能
     */
    @Override
    public void onBackPressed() {
        if (checkCallAgain(2000)){
            //上面的方法，第一次调用时返回false。参数接受一个长整型，代表超时时间，单位ms。
            // 在该时间内调用第二次，则返回true
            System.exit(0);
        } else {
            NoticeUtil.get().showToast("再次点击返回键关闭应用");
        }
    }

    // 工具类使用
    // 基本上所有工具类都可以用get() 和 new Instance(Context) 两种方式获取
    // 若尚未配置FoxCore，get()方法会报错

    /**
     * 检查文字
     * @param text  待查文字
     */
    private void textCheckExample(String text){
        List<Boolean> booleans = new ArrayList<>();
        booleans.add(TextTools.hasChinese(text));   //有汉字
        booleans.add(TextTools.isDouble(text));   //是双精浮点数
        booleans.add(TextTools.isInteger(text));   //是整形数
        booleans.add(TextTools.isObb(3));   //是奇数
        booleans.add(TextTools.isObb("1"));   //是奇数

        //身份证相关
        TextTools.IDCard idCard = TextTools.idCard(text);
        booleans.add(idCard.isLegal()); //是否合法
        booleans.add(idCard.isMale());  //是否是男性
        String cityName = idCard.getCityName(); //获取城市名（中文）
        Date date = idCard.getBirthDay();   //获取生日
    }

    /**
     * 图片相关
     * @param imageFile  总之是个图片文件
     */
    @RequiresPermission(allOf = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    private void requestImageExample(File imageFile){
        //通过系统相机相册剪裁获取图片
        try (RxImagePicker picker = RxImagePicker.get(getSupportFragmentManager(), new Observer<RxImagePicker.Result>() {
            @Override
            public void onSubscribe(Disposable d) {
                dContainer.add(d);  //加入回收器
            }

            @Override
            public void onNext(RxImagePicker.Result result) {
                File imageFile = result.getImageFile();   //获取文件
                int requestCode = result.getRequestCode();  //请求码
                int resultCode = result.getResultCode();    //结果码
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        })){
//            picker.openCamera();    //开启相机
//            picker.openGallery();   //开启相册
            picker.openCrop(imageFile);  //剪裁目标图片
        } catch (IOException e){
            e.printStackTrace();
        }

        //图片工具类, 下面是加载图片文件的例子
        new Thread(() -> {
            //在主线程中运行会报错，因为是耗时操作
            ImageUtil util = ImageUtil.get();
            try {
                Bitmap bitmap = util.decode(imageFile).decodeBitmap();  //加载Bitmap
                Drawable drawable = util.decode(imageFile).decodeDrawable();    //加载Drawable
                //其它操作
                util.decode(imageFile)
                        .roundCorners(8, 8) //圆角化，单位是px
                        .decodeDrawable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 交互
     */
    @RequiresPermission(allOf = {Manifest.permission.VIBRATE})
    private void noticeExample(String notice){
        //显示Toast, 默认LENGTH_LONG, 用该方法产生的Toast会立即覆盖掉同样是该方法产生的上一个Toast
        NoticeUtil.get().showToast(notice);

        //震动功能，需要权限
        NoticeUtil.get().vibrate().vibrate(300);
    }

    /**
     * 获取手机状态
     */
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_NETWORK_STATE})
    private void phoneStatusExample(){
        PhoneStatusUtil util = PhoneStatusUtil.get();
        long freeMem = util.getFreeMemKB();    //获取当前应用可用内存，单位KB
        boolean gps = util.isGPSEnabled(); //是否开启GPS
        boolean netAvailable = util.isNetworkAvailable(); //网络是否可用
        util.showSoftKeyborad(editText);    //以editText为焦点打开键盘

        //网络回调
        ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback(){
            //实现方法
        };
        util.registerNetworkListener(callback); //注册网络状态监听
        util.unregisterNetworkListener(callback); //注销网络状态监听
    }
}
