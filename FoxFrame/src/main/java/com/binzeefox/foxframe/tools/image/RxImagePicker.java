package com.binzeefox.foxframe.tools.image;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.binzeefox.foxframe.core.FoxCore;
import com.binzeefox.foxframe.core.base.FoxApplication;
import com.binzeefox.foxframe.core.tools.ActivityRequestUtil;
import com.binzeefox.foxframe.core.tools.RequestUtil;
import com.binzeefox.foxframe.tools.RxUtil;
import com.binzeefox.foxframe.tools.resource.FileUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

/**
 * 系统获取图片工具类 Rx版
 * <p>
 * {RxImagePicker.get(authority, context, manager, fileObserver).openCamera();}
 * {RxImagePicker.get(manager, fileObserver).openCamera();}
 *
 * @author binze
 * 2019/12/11 11:35
 * @see com.binzeefox.foxframe.tools.image.ImagePicker
 * <p>
 * 2020/09/09 14:30
 * 大改 不再继承自ImagePicker
 * {RxImagePicker.with(this).authority(authority).openCamera}
 * 返回Observable
 */
public class RxImagePicker {
    private static final String TAG = "RxImagePicker";
    private final RequestUtil mUtil;    //请求工具类

    /**
     * 私有化构造器
     *
     * @author 狐彻 2020/09/08 12:48
     */
    private RxImagePicker(final FragmentManager manager) {
        mUtil = RequestUtil.with(manager);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 静态获取
    ///////////////////////////////////////////////////////////////////////////

    public static RxImagePicker with(AppCompatActivity activity) {
        return new RxImagePicker(activity.getSupportFragmentManager());
    }

    public static RxImagePicker with(Fragment fragment) {
        return new RxImagePicker(fragment.getChildFragmentManager());
    }

    ///////////////////////////////////////////////////////////////////////////
    // 参数方法
    ///////////////////////////////////////////////////////////////////////////


    /**
     * 开启相机
     *
     * @author 狐彻 2020/09/08 13:15
     */
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA})
    public Observable<Result> openCamera(int requestCode) {
        return openCamera(requestCode, null);
    }

    /**
     * 开启相机
     *
     * @param targetUri 目标Uri，若非空则返回值中data为空
     * @author 狐彻 2020/09/08 13:15
     */
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA})
    public Observable<Result> openCamera(int requestCode, @Nullable final Uri targetUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (targetUri != null)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
        return mUtil.intentRequest(intent, requestCode)
                .map(new Function<RequestUtil.Result, Result>() {
                    @Override
                    public Result apply(RequestUtil.Result result) throws Exception {
                        Result r = new Result(result);
                        if (targetUri != null) r.imageUri = targetUri;
                        return r;
                    }
                });
    }

    /**
     * 开启相册
     *
     * @author 狐彻 2020/09/08 13:20
     */
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public Observable<Result> openGallery(int requestCode) {
        Intent intent = new Intent
                (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(FoxCore.getApplication().getPackageManager()) != null)
            return mUtil.intentRequest(intent, requestCode)
                    .map(new Function<RequestUtil.Result, Result>() {
                        @Override
                        public Result apply(RequestUtil.Result result) throws Exception {
                            Result r = new Result(result);
                            if (result.getResultCode() == RESULT_OK)
                                r.imageUri = result.getData().getData();
                            return r;
                        }
                    });
        else throw new RuntimeException(TAG + "openGallery: 未知错误");
    }

    /**
     * 开启剪裁
     *
     * @param out   输出Uri
     * @author 狐彻 2020/09/09 14:20
     */
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public Observable<Result> openCrop(Uri in, @Nullable Uri out, int requestCode) {
        return openCrop(in, out, requestCode, null);
    }

    /**
     * 开启剪裁
     *
     * @param out   输出Uri
     * @param interceptor 参数拦截器，若为空则设置为常规参数
     * @author 狐彻 2020/09/09 14:20
     */
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public Observable<Result> openCrop(Uri in, @Nullable final Uri out, int requestCode, @Nullable CropOptionInterceptor interceptor) {
        if (in == null)
            throw new RuntimeException(TAG + " openCrop: 开启剪裁失败，原文件为空");

        Intent intent = new Intent("com.android.camera.action.CROP");
        if (interceptor == null) {
            //通用设置
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 0);//自由比例
            intent.putExtra("aspectY", 0);//自由比例
            intent.putExtra("scale", true);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("return-data", false);  //不要返回Bitmap
            intent.putExtra("noFaceDetection", true);   //取消面部识别
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, out);
            intent.setDataAndType(in, "image/*");
        } else interceptor.intercept(intent);
        if (intent.resolveActivity(FoxCore.getApplication().getPackageManager()) == null)
            throw new RuntimeException(TAG + "openCrop: 未知错误");
        return mUtil.intentRequest(intent, requestCode)
                .map(new Function<RequestUtil.Result, Result>() {
                    @Override
                    public Result apply(RequestUtil.Result result) throws Exception {
                        Result r = new Result(result);
                        if (out != null) r.imageUri = out;
                        return r;
                    }
                });
    }


    ///////////////////////////////////////////////////////////////////////////
    // 内部类
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 结果类
     *
     * @author 狐彻 2020/09/08 12:44
     */
    public static class Result extends RequestUtil.Result {
        private Uri imageUri;

        protected Result(Intent data, int resultCode, int requestCode) {
            super(data, resultCode, requestCode);
        }

        protected Result(RequestUtil.Result result) {
            super(result.getData(), result.getResultCode(), result.getRequestCode());
        }

        public Uri getImageUri() {
            return imageUri;
        }
    }

    /**
     * 剪裁图片操作的拦截器
     *
     * @author 狐彻 2020/09/09 16:07
     */
    public interface CropOptionInterceptor {
        void intercept(Intent intent);
    }
}
