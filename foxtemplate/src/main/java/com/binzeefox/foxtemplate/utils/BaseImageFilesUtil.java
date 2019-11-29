package com.binzeefox.foxtemplate.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.binzeefox.foxtemplate.base.FoxActivity;

import java.io.File;

import androidx.core.content.FileProvider;

import static android.app.Activity.RESULT_OK;

/**
 * 图片获取基类
 *
 * @deprecated 改用添加Fragment的方式接管onActivityResult 的 {@link ImageFilesUtilInterface}
 * @author binze
 * 2019/10/28 13:41
 */
public abstract class BaseImageFilesUtil {
    private final String AUTHORITY;

    public static final int REQUEST_CAMERA = 0xFF;
    public static final int REQUEST_ALBUM = 0xFE;
    public static final int REQUEST_CROP = 0xFD;


    private FoxActivity mCtx;
    protected String tempPath;
    protected File tempFile;
    protected boolean isNougat;   // 是否为7.0
    protected boolean isCrop = false; // 是否剪裁，默认否

    
    /**
     * 接管结果处理
     * @param requestCode   请求码
     * @param resultCode    结果码
     * @param data  返回数据
     * @return  返回获取的文件
     * @author binze 2019/10/28 13:58
     */
    public File onResult(int requestCode, int resultCode, Intent data){
        File file = null;
        if (resultCode != RESULT_OK)
            return null;
        else if (requestCode == REQUEST_CROP){   //剪裁
            if (tempFile != null && tempFile.exists())
                tempFile.delete();
            return new File(tempPath);
        }
        else if (requestCode == REQUEST_CAMERA) {    //相机
            file = new File(tempPath);
            if (!isCrop) return file;
            else tempFile = file;
        }
        else if (requestCode == REQUEST_ALBUM) {     //相册，放弃4.4以下情况
            if (data == null)
                return null;
            Uri rawUri = data.getData();
            file = FileUtil.getImageFileFromUri(getContext(), rawUri);
            if (!isCrop) return file;
            else tempFile = null;
        } else return null;
        openCrop(file);
        return null;
    }

    /**
     * 设置Authority
     * @author binze 2019/10/28 14:04
     */
    protected abstract String getAuthority();

    /**
     * 私有化构造器
     * @author binze 2019/10/28 13:50
     */
    protected BaseImageFilesUtil(FoxActivity ctx){
        mCtx = ctx;
        isNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
        AUTHORITY = getAuthority();
    }

    protected FoxActivity getContext(){
        return mCtx;
    }

    /**
     * 是否剪裁，默认否
     * @author binze 2019/10/28 13:51
     */
    public void setCrop(boolean crop){
        isCrop = crop;
    }

    /**
     * 开启相机
     * @author binze 2019/10/28 13:51
     */
    public void openCamera(){
        tempPath = null;
        tempPath = FileUtil.getImageTempPath(mCtx);
        if (TextUtils.isEmpty(tempPath))
            return;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (!isNougat) { //非7.0
            intent.putExtra(MediaStore.EXTRA_OUTPUT
                    , Uri.fromFile(new File(tempPath)));
        } else {    //7.0环境
            intent.putExtra(MediaStore.EXTRA_OUTPUT
                    , FileProvider.getUriForFile(mCtx, AUTHORITY
                            , new File(tempPath)));
        }
        mCtx.startActivityForResult(intent, REQUEST_CAMERA);
    }

    /**
     * 开启相册
     * @author binze 2019/10/28 13:52
     */
    public void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK
                , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(mCtx.getPackageManager()) != null)
            mCtx.startActivityForResult(intent, REQUEST_ALBUM);
    }

    /**
     * 开始剪裁
     * @author binze 2019/10/28 14:01
     */
    protected void openCrop(File file) {
        if (file == null) return;
        tempPath = FileUtil.getCropTempPath(mCtx);
        Uri outUri; //输出路径
        Uri inUri;  //输入路径
        Intent intent = new Intent("com.android.camera.action.CROP");
        //通用设置
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);//自由比例
        intent.putExtra("aspectY", 0);//自由比例
        intent.putExtra("scale", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);  //不要返回Bitmap
        intent.putExtra("noFaceDetection", true);   //取消面部识别
        if (isNougat) {
            //7.0配置
            outUri = FileUtil.getContentUri(mCtx, tempPath);
            inUri = FileProvider.getUriForFile(mCtx, AUTHORITY, file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            outUri = Uri.parse(tempPath);
            inUri = Uri.fromFile(file);
        }
        intent.setDataAndType(inUri, "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
        mCtx.startActivityForResult(intent, REQUEST_CROP);
    }
}
