package com.binzeefox.foxframe.tools.resource;

import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.binzeefox.foxframe.core.FoxCore;
import com.binzeefox.foxframe.tools.dev.LogUtil;

import java.io.File;
import java.io.IOException;

/**
 * 文件工具类
 *
 */
public class FileUtil {
    private static final String TAG = "FileUtil";

    /**
     * 获取缓存文件夹
     *
     * @author 狐彻 2020/09/10 8:38
     */
    public static File getCacheDir() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            return FoxCore.getApplication().getExternalCacheDir();
        return FoxCore.getApplication().getCacheDir();
    }

    /**
     * 获取缓存文件夹并在其内新建文件夹
     *
     * @author 狐彻 2020/09/10 8:43
     */
    public static File getCacheDir(@Nullable String folderName) {
        File dir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            dir = FoxCore.getApplication().getExternalCacheDir();
        else dir = FoxCore.getApplication().getCacheDir();
        if (folderName == null || TextUtils.isEmpty(folderName)) return dir;

        if (dir == null) return null;

        File[] childFiles = dir.listFiles();
        if (childFiles != null) {
            for (File file : childFiles)
                if (folderName.equals(file.getName()))
                    return file;
        }
        File dir1 = new File(dir.getName() + "/" + folderName);
        if (dir1.mkdir())
            return dir1;

        LogUtil.e(TAG, "getCacheDir: 创建缓存文件夹失败" );
        return null;
    }

    /**
     * 获取缓存文件
     *
     * @author 狐彻 2020/09/10 8:51
     */
    public static File getCacheFile(@Nullable String folderName, @NonNull String fileName){
        File dir = getCacheDir(folderName);
        if (dir == null) return null;

        File[] childFiles = dir.listFiles();
        if (childFiles != null) {
            for (File file : childFiles)
                if (fileName.equals(file.getName()))
                    file.delete();
        }

        File file = new File(dir.getName() + "/" + fileName);
        try {
            if (file.createNewFile())
                return file;
            LogUtil.e(TAG, "getCacheFile: 创建缓存文件失败" );
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
