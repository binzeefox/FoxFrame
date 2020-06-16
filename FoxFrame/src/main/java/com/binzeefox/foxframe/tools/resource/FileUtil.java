package com.binzeefox.foxframe.tools.resource;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件工具类
 *
 * 照片存放在/image内，以当前毫秒命名
 * 剪切图存放在/crop
 * 缓存存放在/temp
 */
public class FileUtil {
    private static final String TAG = "FileUtil";

    //存储文件AUTH
    private static final String ENTERNAL_STORAGE_AUTHORITY
            = "com.android.externalstorage.documents";
    //下载文件AUTH
    private static final String DOWNLOAD_AUTHORITY
            = "com.android.providers.downloads.documents";
    //媒体文件AUTH
    private static final String MEDIA_AUTHORITY
            = "com.android.providers.media.documents";
    //？？文件AUTH
    private static final String GOOGLE_PHOTOS_AUTHORITY
            = "com.google.android.apps.photos.content";
    //下载文件AUTH
    private static final String DOWNLOAD_URI
            = "content://downloads/public_downloads";

    //    ****公共方法 ↓

    /**
     * 获取图片临时存储路径
     * @param context   上下文
     * @return  路径
     */
    public static String getImageTempPath(Context context) {
        String path = getTempDir(context, "/image");
        try {
            path += "/" + System.currentTimeMillis() + ".jpg";
            File temp = new File(path);
            if (temp.exists())
                temp.delete();
            if (!temp.createNewFile()){
                Log.e(TAG, "getImageTempPath: create file failed!!!");
                return null;
            }
        } catch (IOException e) {
            path = null;
        }
        return path;
    }

    /**
     * 获取剪切图缓存路径
     * @param context   上下文
     * @return  路径
     */
    public static String getCropTempPath(Context context){
        String path = getTempDir(context, "/crop");
        try {
            path += "/" + System.currentTimeMillis() + ".jpg";
            File file = new File(path);
            if (file.exists())
                file.delete();
            if (!file.createNewFile()){
                Log.e(TAG, "getCropTempPath: create file failed!!!");
                return null;
            }
        }catch (IOException e){
            path = null;
        }
        return path;
    }

    /**
     * 将本地文件路径转化成FileProvider Uri
     * @param context   上下文
     * @param path  原始路径
     * @return  Uri
     */
    public static Uri getContentUri(Context context, String path){
        File imageFile = new File(path);
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ,new String[]{MediaStore.Images.Media._ID}
                , MediaStore.Images.Media.DATA + "=?"
                ,new String[]{path}
                ,null);

        if (cursor != null && cursor.moveToFirst()){
            //如果已经存在于Provider，则利用id直接生成Uri
            int id = cursor
                    .getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            cursor.close();
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()){
                //如果文件存在于本地，则将其存入Provider
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, path);
                return context.getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }else //图片不存在
                return null;
        }
    }

    /**
     * 通过相册Uri获取图片文件
     * @param rawUri    相册返回的uri
     * @return  目标文件
     */
    public static File getImageFileFromUri(Context context, Uri rawUri) {
        if (DocumentsContract.isDocumentUri(context, rawUri)){
            //若为Document类型，则通过document id处理
            String docId = DocumentsContract.getDocumentId(rawUri);
            if (MEDIA_AUTHORITY.equals(rawUri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                return getFileFromSelection
                        (context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if (DOWNLOAD_AUTHORITY.equals(rawUri.getAuthority())){
                Uri contentUri = ContentUris
                        .withAppendedId(Uri.parse(DOWNLOAD_URI), Long.parseLong(docId));
                return getFileFromSelection(context, contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(rawUri.getScheme())){
            //content类型URI，按照普通方式处理
            return getFileFromSelection(context, rawUri, null);
        } else if (("file".equalsIgnoreCase(rawUri.getScheme()))){
            //文件类型uri，直接获取
            String path = rawUri.getPath();
            return path != null ? new File(rawUri.getPath()) : null;
        }
        return null;
    }

    /**
     * 写入流文件
     * @param is    input stream
     * @param file  目标文件
     */
    public void writeFileStream(InputStream is, File file) throws IOException {
        if (file.exists()) file.delete();
        FileOutputStream fos = null;
        fos = new FileOutputStream(file);
        byte[] bytes = new byte[1024];
        int len;
        while ((len = is.read(bytes)) != -1){
            fos.write(bytes,0,len);
        }
        is.close();
        fos.close();
    }

    /**
     * 复制文件
     * @param fromPath  原文件路径
     * @param toPath    复制后路径
     * @author binze 2019/10/28 16:04
     */
    public static boolean copyFile(String fromPath, String toPath) throws IOException {
        Log.d(TAG, "copyFile: fromPath = " + fromPath + " toPath = " + toPath);
        if (fromPath == null || toPath == null){
            Log.e(TAG, "copyFile: 空参数");
            return false;
        }
        File from = new File(fromPath);
        File copy = new File(toPath);
        if (!from.exists()) {
            Log.e(TAG, "copyFile: 源文件不存在!!!");
            return false;
        }
        if (copy.exists()){
            Log.w(TAG, "copyFile: 目标路径已存在文件, 将被覆盖");
            copy.delete();
        }

        FileInputStream in = new FileInputStream(new File(fromPath));
        FileOutputStream out = new FileOutputStream(new File(toPath));
        byte[] buff = new byte[512];
        int n = 0;
        Log.d(TAG, "copyFile: 复制文件" + fromPath + "到" + toPath);
        while ((n = in.read(buff)) != -1)
            out.write(buff, 0, n);
        out.flush();
        in.close();
        out.close();
        Log.d(TAG, "copyFile: 复制完成");
        return true;
    }

    /**
     * 写入txt
     * @author binze 2019/11/11 10:31
     */
    public static boolean writeTxt(String value, String path){
        FileWriter writer = null;
        File file = new File(path);
        try {
            if (!file.exists()) file.createNewFile();
            writer = new FileWriter(path);
            writer.write(value);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //    ****私有方法 ↓
    private static String getTempDir(Context context, String dirName) {
        String path;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable())
            path = context.getExternalCacheDir().getAbsolutePath();
        else
            path = context.getCacheDir().getAbsolutePath();

        if (path.isEmpty())
            return null;
        path += dirName;
        File dir = new File(path);
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                dir.delete();
                dir.mkdir();
            }
        } else if (!dir.mkdir()){
            Log.e(TAG, "getTempDir: create dir failed" );
            return null;
        }
        return dir.getAbsolutePath();
    }

    /**
     * 通过Uri和Selection获取真实分检
     *
     * @param uri       uri
     * @param selection selection
     * @return 目标文件
     */
    private static File getFileFromSelection(Context ctx, Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = ctx.getContentResolver()
                .query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst())
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return path == null ? null : new File(path);
    }


}
