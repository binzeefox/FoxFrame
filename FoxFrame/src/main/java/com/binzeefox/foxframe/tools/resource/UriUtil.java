package com.binzeefox.foxframe.tools.resource;

import android.content.ContentResolver;
import android.content.ContentValues;
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

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.binzeefox.foxframe.core.FoxCore;
import com.binzeefox.foxframe.tools.dev.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Uri工具类
 *
 * @author 狐彻
 * 2020/09/08 9:50
 */
public class UriUtil {
    private static final String TAG = "UriUtil";

    private UriUtil(){

    }

    public static UriUtil get(){
        return new UriUtil();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 通过Uri获取OutputStream
     *
     * @author 狐彻 2020/09/24 11:11
     */
    public OutputStream getOutputStream(Uri uri) throws FileNotFoundException {
        ContentResolver resolver = FoxCore.getApplication().getContentResolver();
        return resolver.openOutputStream(uri);
    }

    /**
     * 通过Uri获取InputStream
     *
     * @author 狐彻 2020/09/24 11:12
     */
    public InputStream getInputStream(Uri uri) throws FileNotFoundException {
        ContentResolver resolver = FoxCore.getApplication().getContentResolver();
        return resolver.openInputStream(uri);
    }


    /**
     * 根据Uri获取文件类型
     *
     * @author 狐彻 2020/09/24 10:34
     */
    public String getUriMimeType(Uri uri){
        LogUtil.v(TAG, "getUriMimeType: uri = " + uri);
        String type = FoxCore.getApplication()
                .getContentResolver().getType(uri);
        LogUtil.v(TAG, "getUriMimeType: type = " + type);
        return type;
    }

    /**
     * 通过文件获取Uri
     *
     * @author 狐彻 2020/09/09 17:06
     */
    public Uri fileToUri(File file, String authority) {
        return FileProvider.getUriForFile(FoxCore.getApplication(), authority, file);
    }

    /**
     * 通过文件获取FileProvider Uri
     *
     * @author 狐彻 2020/09/10 9:04
     */
    public Uri imageToContentUri(File imageFile){
        Cursor cursor = FoxCore.getApplication().getContentResolver()
                .query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        , new String[]{MediaStore.Images.Media._ID}
                        , MediaStore.Images.Media.DATA + "=?"
                        , new String[]{imageFile.getPath()}
                        , null);

        if (cursor != null && cursor.moveToFirst()){
            //如果已经存在于Provider，则利用id直接生成Uri
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            cursor.close();
            return Uri.withAppendedPath(baseUri, "" + id);
        }
        if (imageFile.exists()){
            //图片文件存在
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, imageFile.getPath());
            return FoxCore.getApplication().getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else return null; //图片不存在
    }

    /**
     * 通过Uri获取文件路径
     *
     * @author 狐彻 2020/09/09 17:08
     */
    public String uriToFilePath(@NonNull Uri uri){
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
            if (TextUtils.isEmpty(path))    //若不是图片文件
                path = convertContentUri(uri);
        }

        return path;
    }

    /**
     * 选取合适应用开启文件
     *
     * @author 狐彻 2020/09/10 10:03
     */
    public void openUriFile(@NonNull Uri uri, @NonNull String mimeType){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);  //临时权限
        intent.setDataAndType(uri, mimeType);
        List<ResolveInfo> resolveInfoList = FoxCore.getApplication().getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : resolveInfoList){
            String packageName = info.activityInfo.packageName;
            FoxCore.getApplication().grantUriPermission(packageName, uri
                    , Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        
        FoxCore.getApplication().startActivity(intent);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 私有方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 处理图片Uri
     *
     * @return 文件路径
     * @author binze 2020/1/14 15:42
     */
    private String convertImageUri(Uri uri) {
        String path = null;
        Cursor cursor = FoxCore.getApplication()
                .getContentResolver()
                .query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null);
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
     * @return 文件路径
     * @author binze 2020/1/14 16:02
     */
    private String convertContentUri(Uri uri) {
        try {
            List<PackageInfo> packs = FoxCore.getApplication().getPackageManager()
                    .getInstalledPackages(PackageManager.GET_PROVIDERS);    //获取系统内所有包
            String fileProviderClassName = FileProvider.class.getName();
            for (PackageInfo pack : packs) {
                ProviderInfo[] providers = pack.providers;  //获取所有Provider
                if (providers == null) {
                    LogUtil.e(TAG, "convertContentUri: no provider find");
                    continue;
                }
                for (ProviderInfo provider : providers) {
                    //遍历Providers找到相同授权的Provider
                    if (!TextUtils.equals(uri.getAuthority(), provider.authority)) continue;
                    if (provider.name.equalsIgnoreCase(fileProviderClassName)) {
                        Class<FileProvider> fileProviderClass = FileProvider.class;
                        Method getPathStrategy = fileProviderClass.getDeclaredMethod("getPathStrategy", Context.class, String.class);
                        getPathStrategy.setAccessible(true);
                        Object invoke = getPathStrategy
                                .invoke(null, FoxCore.getApplication(), uri.getAuthority());
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
            LogUtil.e(TAG, "convertContentUri: ", e);
        }
        return null;
    }
}
