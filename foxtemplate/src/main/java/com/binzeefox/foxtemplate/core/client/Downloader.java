package com.binzeefox.foxtemplate.core.client;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.binzeefox.foxtemplate.core.FoxCore;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.RequiresPermission;

import static android.app.DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR;
import static android.app.DownloadManager.COLUMN_LOCAL_URI;
import static android.app.DownloadManager.COLUMN_STATUS;
import static android.app.DownloadManager.COLUMN_TOTAL_SIZE_BYTES;
import static android.app.DownloadManager.EXTRA_DOWNLOAD_ID;
import static android.app.DownloadManager.Request.NETWORK_MOBILE;
import static android.app.DownloadManager.Request.NETWORK_WIFI;
import static android.app.DownloadManager.Request.VISIBILITY_HIDDEN;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION;

/**
 * 大文件下载工具类
 *
 * @author binze
 * 2019/11/28 9:44
 */
public class Downloader {
    private static final String TAG = "Downloader";
    private static Downloader sInstance = null;
    private final Context mCtx;
    private final DownloadManager mManager;
    private BroadcastReceiver mReceiver;


    /**
     * 静态单例获取
     *
     * @author binze
     * 2019/11/28 10:18
     */
    public static Downloader get(Context ctx) {
        if (sInstance == null) {
            synchronized (Downloader.class) {
                if (sInstance != null) return sInstance;
                else sInstance = new Downloader(ctx);
                return sInstance;
            }
        } else return sInstance;
    }

    /**
     * 静态单例获取
     * <p>
     * 使用的FoxCore注册的Application
     *
     * @author binze
     * 2019/11/28 10:18
     */
    public static Downloader get() {
        return get(FoxCore.getApplication());
    }

    /**
     * 私有化构造器
     *
     * @author binze 2019/11/28 10:18
     */
    private Downloader(Context ctx) {
        mCtx = ctx.getApplicationContext();
        mManager = (DownloadManager) mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    /**
     * 下载
     *
     * @return 下载ID
     * @author binze 2019/11/28 9:50
     */
    @RequiresPermission(allOf = {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    })
    public long download(Params params) {
        DownloadManager.Request request = new DownloadManager.Request(params.downloadUri);
        request.setAllowedNetworkTypes(params.wifiOnly ? NETWORK_WIFI : NETWORK_MOBILE);

        //通知栏通知可见性
        if (params.showNotification && params.showNotificationComplete)
            request.setNotificationVisibility(VISIBILITY_VISIBLE | VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        if (params.showNotification && !params.showNotificationComplete)
            request.setNotificationVisibility(VISIBILITY_VISIBLE);
        if (!params.showNotification && params.showNotificationComplete)
            request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
        if (!params.showNotification && !params.showNotificationComplete)
            request.setNotificationVisibility(VISIBILITY_HIDDEN);

        //通知栏设置
        request.setTitle(params.title);
        request.setDescription(params.description);

        //下载路径
        if (params.fileUri != null) request.setDestinationUri(params.fileUri);
        if (!TextUtils.isEmpty(params.externalFileName))
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, params.externalFileName);

        //开始下载
        return mManager.enqueue(request);
    }

    /**
     * 查询下载状态
     *
     * @author binze 2019/11/28 10:27
     */
    public Query query(long id) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor = mManager.query(query);

        int status = -1;    //下载状态
        int totalBytes = 0;
        int currentBytes = 0;
        String fileUriStr = null;
        if (cursor == null) return null; //无此下载
        if (cursor.moveToFirst()) {
            final Map<String, Integer> columnMap = new HashMap<>();
            columnMap.put("fileUri", cursor.getColumnIndex(COLUMN_LOCAL_URI));
            columnMap.put("totalBytes", cursor.getColumnIndex(COLUMN_TOTAL_SIZE_BYTES));
            columnMap.put("currentBytes", cursor.getColumnIndex(COLUMN_BYTES_DOWNLOADED_SO_FAR));
            Integer colFileUri, colTotal, colCurrent;
            colFileUri = columnMap.get("fileUri");
            colTotal = columnMap.get("totalBytes");
            colCurrent = columnMap.get("currentBytes");

            //取出下载任务状态值
            status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
            //下载任务文件名
            if (colFileUri != null)
                fileUriStr = cursor.getString(colFileUri);
            //文件总大小
            if (colTotal != null)
                totalBytes = cursor.getInt(colTotal);
            //下载总大小
            if (colCurrent != null)
                currentBytes = cursor.getInt(colCurrent);

            cursor.close();
        }
        return new Query(id, status, totalBytes, currentBytes, fileUriStr);
    }

    /**
     * 下载完成监听
     *
     * @author binze 2019/11/28 10:50
     */
    public void setCompleteListener(final OnDownloadCompleteListener listener) {
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1);
                listener.onComplete(id);
            }
        };

        mCtx.registerReceiver(mReceiver, filter);
    }

    /**
     * 移除下载完成监听
     *
     * @author binze 2019/11/28 10:57
     */
    public void removeCompleteListener() {
        if (mReceiver == null) return;
        mCtx.unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    /**
     * 移除下载
     *
     * @author binze 2019/11/28 10:19
     */
    public void removeDownload(long... ids) {
        mManager.remove(ids);
    }

    /**
     * 获取DownloadManager实例
     *
     * @author binze 2019/11/28 10:19
     */
    public DownloadManager getManager() {
        return mManager;
    }

    /**
     * 下载参数类
     * <p>
     * {@link #downloadUri} 下载路径
     * {@link #fileUri} 下载文件保存路径，若不指定则使用系统默认
     * {@link #wifiOnly}    是否仅WIFI下下载，默认为true
     * {@link #showNotification}    是否显示通知栏通知，默认为false
     * {@link #showNotificationComplete}    是否显示下载完成通知，默认为false
     * {@link #title}   通知栏标题
     * {@link #description} 通知栏内容
     * {@link #externalFileName}  默认文件下载保存目录
     *
     * @author binze 2019/11/28 10:01
     */
    public static class Params {
        public Uri downloadUri, fileUri;
        public boolean wifiOnly = true, showNotification = false, showNotificationComplete = false;
        public String title, description, externalFileName;

        public Params(Uri downloadUri) {
            this.downloadUri = downloadUri;
        }
    }

    /**
     * 查询类
     * <p>
     * {@link #id} 下载ID
     * {@link #status} 下载状态
     * {@link #totalBytes}    文件总大小
     * {@link #currentBytes}    当前下载量
     * {@link #fileUriStr}    文件Uri（字符串）
     *
     * @author binze 2019/11/28 10:47
     */
    public static class Query {
        private long id;
        private int status = -1;    //下载状态
        private int totalBytes = 0;
        private int currentBytes = 0;
        private String fileUriStr = null;

        private Query(long id, int status, int totalBytes, int currentBytes, String fileUriStr) {
            this.id = id;
            this.status = status;
            this.totalBytes = totalBytes;
            this.currentBytes = currentBytes;
            this.fileUriStr = fileUriStr;
        }

        public long getId() {
            return id;
        }

        public int getStatus() {
            return status;
        }

        public int getTotalBytes() {
            return totalBytes;
        }

        public int getCurrentBytes() {
            return currentBytes;
        }

        public String getFileUriStr() {
            return fileUriStr;
        }
    }

    /**
     * 下载完成监听
     *
     * @author binze 2019/11/28 10:53
     */
    public interface OnDownloadCompleteListener {
        void onComplete(final long id);
    }
}
