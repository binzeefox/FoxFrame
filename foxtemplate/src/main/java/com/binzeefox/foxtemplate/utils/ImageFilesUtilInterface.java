package com.binzeefox.foxtemplate.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.binzeefox.foxtemplate.R;
import com.binzeefox.foxtemplate.base.FoxApplication;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * 图片获取基类
 * <p>
 * {@link #REQUEST_CAMERA}  系统相机请求码
 * {@link #REQUEST_ALBUM}   系统相册请求码
 * {@link #REQUEST_CROP}    系统剪裁请求码
 * {@link #getAuthority()}  获取授权码
 * {@link #setCrop(boolean)}    是否开启剪裁
 * {@link #openCamera(AppCompatActivity)}{@link #openCamera(Fragment)}   开启相机
 * {@link #openGallery(AppCompatActivity)}{@link #openGallery(Fragment)}    开启相册
 * {@link #openCrop(File, AppCompatActivity)}{@link #openCrop(File, Fragment)}  开始剪裁，参数为需要剪裁的文件
 * {@link #getCameraIntent(String)}   获取相机Intent    参数为路径
 * {@link #getGalleryIntent()}  获取相册Intent
 * {@link #getCropIntent()} 获取剪裁Intent
 *
 * @author binze
 * 2019/11/29 16:08
 */
public interface ImageFilesUtilInterface {
    int REQUEST_CAMERA = R.id.request_camera;
    int REQUEST_ALBUM = R.id.request_album;
    int REQUEST_CROP = R.id.request_crop;

    String getAuthority();

    void setCrop(boolean crop);

    void openCamera(AppCompatActivity activity);
    void openCamera(Fragment fragment);

    void openGallery(AppCompatActivity activity);
    void openGallery(Fragment fragment);

    void openCrop(File file, AppCompatActivity activity);
    void openCrop(File file, Fragment fragment);

    Intent getCameraIntent(String path);

    Intent getGalleryIntent();

    Intent getCropIntent();
}
