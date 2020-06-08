package com.binzeefox.foxframe.tools.image;

import java.io.File;

/**
 * 系统获取图片接口类
 * @author binze
 * 2019/12/11 11:19
 */
public interface ImagePicker {

    /**
     * 开启相机
     * @author binze 2019/12/11 11:34
     */
    void openCamera();

    /**
     * 开启相册
     * @author binze 2019/12/11 11:34
     */
    void openGallery();

    /**
     * 开启剪裁
     * @author binze 2019/12/11 11:35
     */
    void openCrop(File file);
}
