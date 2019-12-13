package com.binzeefox.foxtemplate.tools.image;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.binzeefox.foxtemplate.core.FoxCore;
import com.binzeefox.foxtemplate.core.base.FoxApplication;
import com.binzeefox.foxtemplate.core.tools.ActivityRequestUtil;
import com.binzeefox.foxtemplate.tools.resource.FileUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.content.FileProvider;
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
 * @see com.binzeefox.foxtemplate.tools.image.ImagePicker
 */
public class RxImagePicker implements ImagePicker, Closeable {
    private static final String TAG = "RxImagePicker";
    private CompositeDisposable dContainer = new CompositeDisposable(); //回收器
    private static String sAuthority;
    @SuppressLint("StaticFieldLeak")
    private static Context sContext;    //Application Context
    private Observer<Result> observer = null;    //当前操作的observer
    private FragmentManager manager = null; //设置FragmentManager
    private String curPath = null;

    /**
     * 私有化构造器
     *
     * @author binze 2019/12/11 11:40
     */
    private RxImagePicker(FragmentManager manager, Observer<Result> fileObserver) {
        observer = fileObserver;
        this.manager = manager;
    }

    /**
     * 静态单例(初始化)
     *
     * @param authority 内容提供者授权，也可以通过重写{@link FoxApplication#getAuthority()}进行设置
     * @param context   上下文
     * @author binze 2019/12/11 11:40
     */
    public static RxImagePicker get(@NonNull String authority, @NonNull Context context
            , @NonNull FragmentManager manager, @NonNull Observer<Result> fileObserver) {
        sAuthority = authority; //初始化authority
        sContext = context.getApplicationContext();
        return new RxImagePicker(manager, fileObserver);
    }

    /**
     * 静态单例
     *
     * @author binze 2019/12/11 11:46
     */
    public static RxImagePicker get(@NonNull FragmentManager manager, @NonNull Observer<Result> fileObserver) {
        sContext = FoxCore.getApplication();
        if (TextUtils.isEmpty(sAuthority)) return new RxImagePicker(manager, fileObserver);
        if (sContext instanceof FoxApplication)
            sAuthority = ((FoxApplication) sContext).getAuthority();
        if (TextUtils.isEmpty(sAuthority)) return new RxImagePicker(manager, fileObserver);

        //若无authority则报错
        throw new RuntimeException(
                new IllegalAccessException(
                        "尚未设置authority, 请调用 RxImagePicker#get(String)或重写" +
                                " FoxApplication#getAuthority()方法")
        );
    }

    @Override
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA})
    public void openCamera() {
        Intent intent = getCameraIntent();
        ActivityRequestUtil.init(intent).request(manager)
                .compose(setMapFunc())
                .subscribe(observer);
    }

    @Override
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void openGallery() {
        String path = FileUtil.getImageTempPath(sContext);
        Intent intent = getGalleryIntent();
        if (intent.resolveActivity(sContext.getPackageManager()) != null)
            ActivityRequestUtil.init(intent).request(manager)
            .compose(setMapFunc())
            .subscribe(observer);
    }

    @Override
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void openCrop(File file) {
        if (file == null) return;
        String path = FileUtil.getCropTempPath(sContext);
        Uri inUri = FileProvider.getUriForFile(sContext, sAuthority, file);
        Uri outUri = FileUtil.getContentUri(sContext, path);

        Intent intent = getCropIntent(inUri, outUri);
        ActivityRequestUtil.init(intent).request(manager)
                .compose(setMapFunc())
                .subscribe(observer);
    }

    /**
     * 获取开启相机Intent
     *
     * @author binze 2019/12/11 12:27
     */
    protected Intent getCameraIntent() {
        curPath = FileUtil.getImageTempPath(sContext);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT
                , FileProvider.getUriForFile(sContext, sAuthority
                        , new File(curPath)));
        return intent;
    }

    /**
     * 获取开启相册Intent
     *
     * @author binze 2019/12/11 12:29
     */
    protected Intent getGalleryIntent() {
        return new Intent(Intent.ACTION_PICK
                , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    /**
     * 获取剪裁Intent
     *
     * @author binze 2019/12/11 12:29
     */
    protected Intent getCropIntent(Uri inUri, Uri outUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //通用设置
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);//自由比例
        intent.putExtra("aspectY", 0);//自由比例
        intent.putExtra("scale", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);  //不要返回Bitmap
        intent.putExtra("noFaceDetection", true);   //取消面部识别
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
        intent.setDataAndType(inUri, "image/*");

        return intent;
    }

    @Override
    public void close() throws IOException {
        dContainer.dispose();
        dContainer.clear();
        dContainer = null;
        observer = null;
    }

    /**
     * 数据处理
     *
     * @author binze 2019/12/11 12:42
     */
    protected ObservableTransformer<ActivityRequestUtil.Result, Result> setMapFunc() {
        return new ObservableTransformer<ActivityRequestUtil.Result, Result>() {
            @Override
            public ObservableSource<Result> apply(Observable<ActivityRequestUtil.Result> upstream) {
                return upstream
                        .map(new Function<ActivityRequestUtil.Result, Result>() {
                            @Override
                            public Result apply(ActivityRequestUtil.Result result) throws Exception {
                                Result r = new Result(result.getRequestCode(), result.getResultCode(), null);
                                if (result.getResultCode() == RESULT_OK) {   //获取成功
                                    r.imageFile = new File(curPath);
                                }
                                return r;
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 结果类
     *
     * @author binze
     * 2019/11/29 17:33
     */
    public static class Result {
        private int requestCode;
        private int resultCode;
        private File imageFile;

        private Result(int requestCode, int resultCode, File imageFile) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.imageFile = imageFile;
        }

        public int getRequestCode() {
            return requestCode;
        }

        public int getResultCode() {
            return resultCode;
        }

        public File getImageFile() {
            return imageFile;
        }
    }
}
