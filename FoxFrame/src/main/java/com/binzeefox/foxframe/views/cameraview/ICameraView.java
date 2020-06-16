package com.binzeefox.foxframe.views.cameraview;

/**
 * TODO CameraView 接口
 * @author binze
 * 2020/6/15 15:01
 */
public interface ICameraView {

    /**
     * 初始化，准备并开启相机
     * @author binze 2020/6/15 15:07
     */
    void initCamera();

    /**
     * 开启预览
     * @author binze 2020/6/15 15:07
     */
    void previewOn();

    /**
     * 关闭预览
     * @author binze 2020/6/15 15:08
     */
    void previewOff();

    /**
     * 拍摄
     * @author binze 2020/6/15 15:09
     */
    void takeShot();

    /**
     * 录像
     * @author binze 2020/6/15 15:09
     */
    void record();

    /**
     * 录像结束
     * @author binze 2020/6/15 15:09
     */
    void endRecord();

    /**
     * 释放相机
     * @author binze 2020/6/15 15:11
     */
    void closeCamera();
}
