package com.binzeefox.foxframe.views.cameraview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

/**
 * TODO 全自动简易CameraView
 *
 * @author binze
 * 2020/6/15 14:58
 */
public class TextureCameraView extends TextureView implements ICameraView {
    private static final String TAG = "TextureCameraView";
    public static final int FACING_FRONT = 0;   //后置
    public static final int FACING_BACK = 1;    //前置

    private Context mCtx;   //上下文实例
    private CameraUtil mUtil;   //相机工具类
    private String[] mCameraIds = new String[0];    //相机ID列表
    private int cameraFacing = FACING_FRONT;    //默认后置摄像头

    public TextureCameraView(Context context) {
        super(context);
        init(context);
    }

    public TextureCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextureCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化
     *
     * @author binze 2020/6/15 15:46
     */
    private void init(Context ctx) {
        mCtx = ctx;
        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                initCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void initCamera() {
        if (mUtil == null) {  //初始化管理器
            CameraManager manager = (CameraManager) mCtx.getSystemService(Context.CAMERA_SERVICE);
            mUtil = new CameraUtil(manager);
        }
        mCameraIds = mUtil.cameraIDs();
    }

    @Override
    public void previewOn() {
        try {
            if (cameraFacing == FACING_FRONT)    //TODO 后置摄像头
                previewFront();
            else if (cameraFacing == FACING_BACK)    //TODO 前置摄像头
                previewBack();
        } catch (CameraAccessException e) {
            Log.e(TAG, "previewOn: 开启预览失败", e);
        }
    }

    @Override
    public void previewOff() {

    }

    @Override
    public void takeShot() {

    }

    @Override
    public void record() {

    }

    @Override
    public void endRecord() {

    }

    @Override
    public void closeCamera() {

    }

    /**
     * 开启后置摄像头
     *
     * @author binze 2020/6/16 14:03
     */
    private void previewFront() throws CameraAccessException {
        for (String id : mCameraIds) {
            CameraCharacteristics cc = mUtil.cameraManager().getCameraCharacteristics(id);
            if (cc.get(CameraCharacteristics.LENS_FACING)
                    == CameraCharacteristics.LENS_FACING_FRONT) {
                openCamera(cc);
                break;
            }
        }
    }

    /**
     * 开启前置摄像头
     *
     * @author binze 2020/6/16 14:03
     */
    private void previewBack() throws CameraAccessException {
        for (String id : mCameraIds) {
            CameraCharacteristics cc = mUtil.cameraManager().getCameraCharacteristics(id);
            if (cc.get(CameraCharacteristics.LENS_FACING)
                    == CameraCharacteristics.LENS_FACING_BACK) {
                openCamera(cc);
                break;
            }
        }
    }

    /**
     * 开启指定摄像头
     *
     * @author binze 2020/6/16 14:16
     */
    private void openCamera(CameraCharacteristics cc) {
        //获取输出格式和预览尺寸
        StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Object o = map.getOutputSizes(SurfaceTexture.class);
        Log.d(TAG, "openCamera: " + o);
    }
}
