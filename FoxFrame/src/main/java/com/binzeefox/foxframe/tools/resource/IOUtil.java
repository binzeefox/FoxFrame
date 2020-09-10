package com.binzeefox.foxframe.tools.resource;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.binzeefox.foxframe.core.FoxCore;
import com.binzeefox.foxframe.core.tools.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 输入/输出 工具类
 * @author binze
 * 2020/6/15 14:30
 */
public class IOUtil {
    private static final String TAG = "IOUtil";

    /**
     * 流入流
     *
     * @author 狐彻 2020/09/10 10:14
     */
    public static void streamToStream(@NonNull InputStream is, @NonNull OutputStream os) throws IOException{
        byte[] buffer = new byte[1024]; //缓存池
        int index = 0;
        while ((index = is.read(buffer)) != -1){
            os.write(buffer, 0, index);
            os.flush();
        }
    }

    /**
     * bytes入流
     *
     * @author 狐彻 2020/09/10 10:41
     */
    public static void bytesToSteam(@NonNull byte[] bytes, @NonNull OutputStream os) throws IOException{
        os.write(bytes);
        os.flush();
    }

    /**
     * 读取txt文件内容
     *
     * @author 狐彻 2020/09/10 10:43
     */
    public static String readTextFile(@NonNull File file) throws IOException {
        if (!file.exists()){
            LogUtil.i(TAG, "readTextFile: 源文件不存在");
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file)){
            byte[] buffer = new byte[1024]; //缓存池
            StringBuilder sb = new StringBuilder();
            int index = 0;
            while ((index = fis.read()) != -1)
                sb.append(new String(buffer));

            return sb.toString();
        }
    }

    /**
     * 复制文件
     *
     * @author 狐彻 2020/09/10 10:29
     */
    public static void copyFile(@NonNull File source, @NonNull File target) throws IOException{
        if (!source.exists()) {
            LogUtil.i(TAG, "copyFile: 原始文件不存在");
            return;
        }

        if (target.exists()) {
            LogUtil.i(TAG, "copyFile: 目标位置已存在文件，请确认并删除后重试");
            return;
        }

        if (!target.createNewFile()){
            LogUtil.w(TAG, "copyFile: 创建文件失败");
            return;
        }

        try (FileReader fr = new FileReader(source);
             FileWriter fw = new FileWriter(target)){

            char[] buffer = new char[1024];
            int index = 0;
            while ((index = fr.read(buffer)) != -1){
                fw.write(buffer, 0, index);
                fw.flush();
            }
        }
    }

    /**
     * 将文件写入Uri
     *
     * @author 狐彻 2020/09/10 9:48
     */
    public static void writeToUri(@NonNull FileInputStream is, @NonNull Uri targetUri ) throws IOException {
        ContentResolver resolver = FoxCore.getApplication().getContentResolver();
        try (OutputStream os = resolver.openOutputStream(targetUri)){
            if (os == null) {
                LogUtil.i(TAG, "writeToUri: 获取输出流失败");
                return;
            }
            int read;
            byte[] buffer = new byte[1024];
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
                os.flush();
            }
        }
    }
}
