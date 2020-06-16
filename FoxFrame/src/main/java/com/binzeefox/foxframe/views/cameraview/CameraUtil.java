package com.binzeefox.foxframe.views.cameraview;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;

import com.binzeefox.foxframe.core.FoxCore;

import androidx.annotation.NonNull;

/**
 * 相机工具类
 * @author binze
 * 2020/6/15 15:20
 */
public class CameraUtil {
    private static final String TAG = "CameraUtil";
    private final CameraManager mManager;

    public CameraUtil(CameraManager manager){
        mManager = manager;
    }

    public CameraUtil(Context ctx){
        mManager = (CameraManager) ctx.getSystemService(Context.CAMERA_SERVICE);
    }

    public String[] cameraIDs(){
        try {
            if (mManager == null) return new String[0];
            return mManager.getCameraIdList();
        } catch (CameraAccessException e){
            Log.e(TAG, "cameraIDs: 获取CameraID信息失败", e);
            return new String[0];
        }
    }

    /**
     * 获取相机数量
     * @author binze 2020/6/15 15:23
     */
    public int cameraCount(){
        return cameraIDs().length;
    }

    /**
     * 获取管理类实例
     *
     * @author binze 2020/6/15 15:42
     */
    @NonNull
    public CameraManager cameraManager(){
        return mManager;
    }
}
