package com.binzeefox.foxframe.tools.resource;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.binzeefox.foxframe.core.FoxCore;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

/**
 * File与Uri间互相获取
 * @author binze
 * 2020/6/15 14:21
 */
public class FileUriUtil {
    private static final String TAG = "FileUriUtil";
    private Context mContext;
    private final String cAuthority;

    /**
     * 构造器
     *
     * @param context   上下文
     * @param authority FileProvider授权
     * @author binze 2020/1/14 15:36
     */
    public FileUriUtil(Context context, String authority) {
        this.mContext = context.getApplicationContext();
        this.cAuthority = authority;
    }

    /**
     * 静态获取
     *
     * @author binze 2020/1/14 15:36
     */
    public static FileUriUtil get(String authority) {
        return new FileUriUtil(FoxCore.getApplication(), authority);
    }

    /**
     * 获取文件Uri
     *
     * @author binze 2020/1/14 15:37
     */
    public Uri fileToUri(File file) {
        return FileProvider.getUriForFile(mContext, cAuthority, file);
    }

    /**
     * 通过Uri获取文件
     *
     * @author binze 2020/1/14 15:38
     */
    public File uriToFile(@NonNull Uri uri) {
        final String scheme = uri.getScheme();  //前缀
        String path = null;

        if (scheme == null) {    //前缀为空，直接获取路径
            path = uri.getPath();
        }
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {    //文件前缀
            path = uri.getPath();
        }
        if (ContentResolver.SCHEME_CONTENT.equals(scheme)) { //Content前缀
            path = convertImageUri(uri);
            if (TextUtils.isEmpty(path))
                path = convertContentUri(uri);
        }

        if (TextUtils.isEmpty(path)) return null;
        return new File(path);
    }

    /**
     * 打开文件
     * @author binze 2020/1/14 16:19
     */
    public void openFile(@NonNull File file, @NonNull String mimeType, int permissionFlag){
        Uri uri = fileToUri(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.setDataAndType(uri, mimeType);
        List<ResolveInfo> resInfoList = mContext.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : resInfoList){
            String packageName = info.activityInfo.packageName;
            mContext.grantUriPermission(packageName, uri, permissionFlag);
        }
        mContext.startActivity(intent);
    }

    /**
     * 处理图片Uri
     *
     * @author binze 2020/1/14 15:42
     */
    private String convertImageUri(Uri uri) {
        String path = null;
        Cursor cursor = mContext.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null);
        if (cursor == null) return null;
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            if (index > -1) {
                path = cursor.getString(index);
            }
        }
        cursor.close();
        return path;
    }

    /**
     * 处理Content Uri
     *
     * @author binze 2020/1/14 16:02
     */
    private String convertContentUri(Uri uri) {
        try {
            List<PackageInfo> packs = mContext.getPackageManager()
                    .getInstalledPackages(PackageManager.GET_PROVIDERS);    //获取系统内所有包
            String fileProviderClassName = FileProvider.class.getName();
            for (PackageInfo pack : packs) {
                ProviderInfo[] providers = pack.providers;  //获取所有Provider
                if (providers == null) {
                    Log.e(TAG, "convertContentUri: no provider find");
                    continue;
                }
                for (ProviderInfo provider : providers) {
                    //遍历Providers找到相同授权的Provider
                    if (!TextUtils.equals(uri.getAuthority(), provider.authority)) continue;
                    if (provider.name.equalsIgnoreCase(fileProviderClassName)) {
                        Class<FileProvider> fileProviderClass = FileProvider.class;
                        Method getPathStrategy = fileProviderClass.getDeclaredMethod("getPathStrategy", Context.class, String.class);
                        getPathStrategy.setAccessible(true);
                        Object invoke = getPathStrategy.invoke(null, mContext, uri.getAuthority());
                        if (invoke != null) {
                            String PathStrategyStringClass = FileProvider.class.getName() + "$PathStrategy";
                            Class<?> PathStrategy = Class.forName(PathStrategyStringClass);
                            Method getFileForUri = PathStrategy.getDeclaredMethod("getFileForUri", Uri.class);
                            getFileForUri.setAccessible(true);
                            Object invoke1 = getFileForUri.invoke(invoke, uri);
                            if (invoke1 instanceof File) {
                                return ((File) invoke1).getAbsolutePath();
                            }
                        }
                        break;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "convertContentUri: ", e);
        }
        return null;
    }
}
