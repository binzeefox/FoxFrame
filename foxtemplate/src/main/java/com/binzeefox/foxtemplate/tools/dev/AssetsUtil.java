package com.binzeefox.foxtemplate.tools.dev;

import android.content.Context;
import android.util.Log;

import com.binzeefox.foxtemplate.core.FoxCore;

import java.io.IOException;
import java.io.InputStream;

/**
 * 操作Assets文件工具类
 *
 * @author binze
 * 2020/1/2 14:38
 */
public class AssetsUtil {
    private static final String TAG = "AssetsUtil";
    private Context mCtx;

    /**
     * 构造器
     * @author binze 2020/1/2 14:42
     */
    public AssetsUtil(Context context){
        mCtx = context.getApplicationContext();
    }

    /**
     * 静态获取
     * @author binze 2020/1/2 14:42
     */
    public static AssetsUtil get(){
        return new AssetsUtil(FoxCore.getApplication());
    }

    /**
     * 读取
     * @author binze 2020/1/2 14:45
     */
    public String readTxtAssets(String fullName) throws IOException {
        try (InputStream is = mCtx.getResources().getAssets().open(fullName)){
            byte[] bytes = new byte[is.available()];
            return new String(bytes);
        } catch (IOException e){
            Log.e(TAG, "readTxtAssets: 读取TXT文件失败", e);
            throw e;
        }
    }
}
