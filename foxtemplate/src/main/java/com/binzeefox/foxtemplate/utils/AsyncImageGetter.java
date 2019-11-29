package com.binzeefox.foxtemplate.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.binzeefox.foxtemplate.R;
import com.binzeefox.foxtemplate.base.FoxApplication;

import java.io.File;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static android.app.Activity.RESULT_OK;

/**
 * 图片获取基类
 *
 * TODO 待测试
 * 应用了{@link PermissionUtil}{@link ActivityRequestUtil} 实现回调
 * @author binze
 * 2019/11/29 16:26
 */
public abstract class AsyncImageGetter implements ImageFilesUtilInterface {
    private final String AUTHORITY; //授权
    private static final FoxApplication mApp = FoxApplication.get();    //Application实例
    private Observable<Result> mObservable; //图片被观察者
    private ObservableEmitter<Result> mEmitter; //上面那个被观察者的事件发射器


    /**
     * 私有化构造器
     * @author binze 2019/11/29 17:46
     */
    protected AsyncImageGetter(){
        AUTHORITY = getAuthority();
        mObservable = Observable
                .create((ObservableOnSubscribe<Result>) emitter -> mEmitter = emitter)
                .compose(RxUtil.setThread());
    }

    /**
     * 订阅事件，调用该方法后才能获取到图片回调
     * @author binze 2019/11/29 17:45
     */
    public void subcribe(Observer<Result> fileObserver){
        mObservable.subscribe(fileObserver);
    }

    /**
     * 开启相机
     * @author binze 2019/11/29 17:25
     */
    @Override
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA})
    public void openCamera(AppCompatActivity activity) {
        final String path = FileUtil.getImageTempPath(mApp);
        Intent intent = getCameraIntent(path);
        ActivityRequestUtil.init(intent).request(activity)
                .subscribe(getImageObserver(path));
    }

    /**
     * 开启相机
     * @author binze 2019/11/29 17:25
     */
    @Override
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA})
    public void openCamera(Fragment fragment) {
        final String path = FileUtil.getImageTempPath(mApp);
        Intent intent = getCameraIntent(path);
        ActivityRequestUtil.init(intent).request(fragment)
                .subscribe(getImageObserver(path));
    }

    /**
     * 开启相册
     * @author binze 2019/11/29 17:25
     */
    @Override
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void openGallery(AppCompatActivity activity) {
        final String path = FileUtil.getImageTempPath(mApp);
        Intent intent = getGalleryIntent();
        if (intent.resolveActivity(mApp.getPackageManager()) != null)
            ActivityRequestUtil.init(intent).request(activity)
                    .subscribe(getImageObserver(path));
    }
    
    /**
     * 开启相册
     * @author binze 2019/11/29 17:25
     */
    @Override
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void openGallery(Fragment fragment) {
        final String path = FileUtil.getImageTempPath(mApp);
        Intent intent = getGalleryIntent();
        if (intent.resolveActivity(mApp.getPackageManager()) != null)
            ActivityRequestUtil.init(intent).request(fragment)
                    .subscribe(getImageObserver(path));
    }

    /**
     * 剪裁
     * @author binze 2019/11/29 17:37
     */
    @Override
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void openCrop(File file, AppCompatActivity activity) {
        if (file == null) return;
        String path = FileUtil.getCropTempPath(mApp);
        Uri inUri = FileProvider.getUriForFile(mApp, AUTHORITY, file);
        Uri outUri = FileUtil.getContentUri(mApp, path);
        
        Intent intent = getCropIntent();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
        intent.setDataAndType(inUri, "image/*");

        ActivityRequestUtil.init(intent).request(activity)
                .subscribe(getImageObserver(path));
    }

    /**
     * 剪裁
     * @author binze 2019/11/29 17:37
     */
    @Override
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void openCrop(File file, Fragment fragment) {
        if (file == null) return;
        String path = FileUtil.getCropTempPath(mApp);
        Uri inUri = FileProvider.getUriForFile(mApp, AUTHORITY, file);
        Uri outUri = FileUtil.getContentUri(mApp, path);

        Intent intent = getCropIntent();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
        intent.setDataAndType(inUri, "image/*");

        ActivityRequestUtil.init(intent).request(fragment)
                .subscribe(getImageObserver(path));
    }

    /**
     * 获取相机Intent
     * @author binze 2019/11/29 17:44
     */
    @Override
    public Intent getCameraIntent(String path) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT
                , FileProvider.getUriForFile(mApp, AUTHORITY
                        , new File(path)));
        return intent;
    }

    /**
     * 获取相册Intent
     * @author binze 2019/11/29 17:44
     */
    @Override
    public Intent getGalleryIntent() {
        return new Intent(Intent.ACTION_PICK
                , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    /**
     * 获取剪裁Intent
     * @author binze 2019/11/29 17:44
     */
    @Override
    public Intent getCropIntent() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //通用设置
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);//自由比例
        intent.putExtra("aspectY", 0);//自由比例
        intent.putExtra("scale", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);  //不要返回Bitmap
        intent.putExtra("noFaceDetection", true);   //取消面部识别
        return intent;
    }
    
    /**
     * 处理ActivityResult的回调
     * @author binze 2019/11/29 17:45
     */
    private Observer<ActivityRequestUtil.Result> getImageObserver(final String path){
        return new Observer<ActivityRequestUtil.Result>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(ActivityRequestUtil.Result result) {
                Result r = new Result(result.requestCode, result.resultCode, null);
                if (result.resultCode == RESULT_OK) {   //获取成功
                    r.imageFile = new File(path);
                }
                mEmitter.onNext(r);
            }

            @Override
            public void onError(Throwable e) {
                mEmitter.onError(e);
            }

            @Override
            public void onComplete() {
                disposable.dispose();
            }
        };
    }

    /**
     * 结果类
     * @author binze
     * 2019/11/29 17:33
     */
    public static class Result{
        public int requestCode;
        public int resultCode;
        public File imageFile;

        private Result(int requestCode, int resultCode, File imageFile) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.imageFile = imageFile;
        }
    }
}
