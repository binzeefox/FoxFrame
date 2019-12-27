package com.binzeefox.foxtemplate.tools.image;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

import com.binzeefox.foxtemplate.core.FoxCore;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

/**
 * 应用ImageDecoder的图片工具类
 *
 * @author binze
 * 2019/12/27 16:09
 */
@TargetApi(Build.VERSION_CODES.P)
public class ImageUtil {
    private static final String TAG = "ImageUtil";
    private Context mCtx;   //上下文实例

    public ImageUtil(@NonNull Context context) {
        mCtx = context.getApplicationContext();
    }

    public ImageUtil get() {
        Context context = FoxCore.getApplication();
        if (context == null)
            throw new RuntimeException(new IllegalAccessError("call FoxCore.init() first!!!"));
        return new ImageUtil(FoxCore.getApplication());
    }

    /**
     * 解码
     *
     * @author binze
     * 2019/12/27 16:14
     */
    public Decoder decode(@NonNull ImageDecoder.Source source) {
        return new Decoder(source);
    }

    public Decoder decode(@IdRes int sourceId) {
        ImageDecoder.Source source = ImageDecoder.createSource(mCtx.getResources(), sourceId);
        return new Decoder(source);
    }

    public Decoder decode(@NonNull Uri uri) {
        ImageDecoder.Source source = ImageDecoder.createSource(mCtx.getContentResolver(), uri);
        return new Decoder(source);
    }

    public Decoder decode(@NonNull ByteBuffer buffer) {
        ImageDecoder.Source source = ImageDecoder.createSource(buffer);
        return new Decoder(source);
    }

    public Decoder decode(@NonNull File file) {
        ImageDecoder.Source source = ImageDecoder.createSource(file);
        return new Decoder(source);
    }

    /**
     * 负责解码的类
     *
     * @author binze 2019/12/27 16:16
     */
    public class Decoder {
        private ImageDecoder.Source source;
        //监听器数组
        private List<ImageDecoder.OnHeaderDecodedListener> decodeListenerList = new ArrayList<>();
        //不完全解码监听器。若触发并返回true，则展示不完整的图片，缺损区域将会是空白
        private ImageDecoder.OnPartialImageListener partialImageListener = null;
        private int sizeIndex = -1; //尺寸设置监听器角标
        private int sampleSizeIndex = -1;   //清晰度监听器角标

        public Decoder(@NonNull ImageDecoder.Source source) {
            this.source = source;
        }

        /**
         * 设置不完整加载监听器
         * @author binze 2019/12/27 16:55
         */
        public Decoder partialImageListener(ImageDecoder.OnPartialImageListener listener){
            partialImageListener = listener;
            return this;
        }
        
        /**
         * 设置头解码监听器
         * @author binze 2019/12/27 16:35
         */
        public Decoder headDecodeListener(ImageDecoder.OnHeaderDecodedListener listener){
            if (!decodeListenerList.contains(listener))
                decodeListenerList.add(listener);
            return this;
        }

        /**
         * 设置图片压缩
         * @author binze 2019/12/27 16:40
         */
        public Decoder setSampleSize(final int sampleSize){
            if (sampleSizeIndex != -1)
                decodeListenerList.remove(sampleSizeIndex);
            sampleSizeIndex = decodeListenerList.size();
            decodeListenerList.add(new ImageDecoder.OnHeaderDecodedListener() {
                @Override
                public void onHeaderDecoded(@NonNull ImageDecoder decoder, @NonNull ImageDecoder.ImageInfo info, @NonNull ImageDecoder.Source source) {
                    decoder.setTargetSampleSize(sampleSize);
                }
            });
            return this;
        }

        /**
         * 设置图片尺寸
         * @author binze 2019/12/27 16:48
         */
        public Decoder setSampleSize(final int width, final int height){
            if (sizeIndex != -1)
                decodeListenerList.remove(sizeIndex);
            sizeIndex = decodeListenerList.size();
            decodeListenerList.add(new ImageDecoder.OnHeaderDecodedListener() {
                @Override
                public void onHeaderDecoded(@NonNull ImageDecoder decoder, @NonNull ImageDecoder.ImageInfo info, @NonNull ImageDecoder.Source source) {
                    decoder.setTargetSize(width, height);
                }
            });
            return this;
        }

        /**
         * 解码Drawable
         *
         * @author binze 2019/12/27 16:31
         */
        public Drawable decodeDrawable() throws IOException {
            return ImageDecoder.decodeDrawable(source, new ImageDecoder.OnHeaderDecodedListener() {
                @Override
                public void onHeaderDecoded(@NonNull ImageDecoder decoder, @NonNull ImageDecoder.ImageInfo info, @NonNull ImageDecoder.Source source) {
                    //不完整加载监听器
                    if (partialImageListener != null)
                        decoder.setOnPartialImageListener(partialImageListener);
                    //遍历已经添加的所有监听器
                    for (ImageDecoder.OnHeaderDecodedListener listener : decodeListenerList)
                        listener.onHeaderDecoded(decoder, info, source);
                }
            });
        }

        /**
         * 解码Bitmap
         *
         * @author binze 2019/12/27 16:32
         */
        public Bitmap decodeBitmap() throws IOException {
            return ImageDecoder.decodeBitmap(source, new ImageDecoder.OnHeaderDecodedListener() {
                @Override
                public void onHeaderDecoded(@NonNull ImageDecoder decoder, @NonNull ImageDecoder.ImageInfo info, @NonNull ImageDecoder.Source source) {
                    //不完整加载监听器
                    if (partialImageListener != null)
                        decoder.setOnPartialImageListener(partialImageListener);
                    //遍历已经添加的所有监听器
                    for (ImageDecoder.OnHeaderDecodedListener listener : decodeListenerList)
                        listener.onHeaderDecoded(decoder, info, source);
                }
            });
        }
    }
}
