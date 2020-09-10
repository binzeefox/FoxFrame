package com.binzeefox.foxframe.tools.resource;

import androidx.annotation.NonNull;

import com.binzeefox.foxframe.tools.dev.LogUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * 异步的IO工具类
 *
 * @author 狐彻
 * 2020/09/10 11:00
 */
public class RxIOUtil extends IOUtil{
    private static final String TAG = "RxIOUtil";

    /**
     * 流入流
     *
     * @return onNext：已完成bytes
     * @author 狐彻 2020/09/10 11:05
     *
     * 可能会造成Stream的回收问题，暂不推荐使用
     */
    public static Observable<Long> streamToStream(@NonNull final InputStream is, @NonNull final OutputStream os) {
        return Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> emitter) throws Exception {
                long progress = 0;
                byte[] buffer = new byte[1024]; //缓存池
                int index = 0;

                while ((index = is.read(buffer)) != -1){
                    os.write(buffer, 0, index);
                    progress += index;
                    emitter.onNext(progress);
                    os.flush();
                }
                emitter.onComplete();
            }
        });
    }

    /**
     * 复制文件
     *
     * @return onNext：已完成bytes
     * @author 狐彻 2020/09/10 11:07
     */
    public static Observable<Long> copyFile(@NonNull final File source, @NonNull final File target) throws IOException {
        if (!source.exists()) {
            LogUtil.i(TAG, "copyFile: 原始文件不存在");
            return null;
        }

        if (target.exists()) {
            LogUtil.i(TAG, "copyFile: 目标位置已存在文件，请确认并删除后重试");
            return null;
        }

        if (!target.createNewFile()){
            LogUtil.w(TAG, "copyFile: 创建文件失败");
            return null;
        }

        return Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> emitter) throws Exception {
                try (FileReader fr = new FileReader(source);
                     FileWriter fw = new FileWriter(target)){

                    char[] buffer = new char[1024];
                    int index = 0;
                    long progress = 0;
                    while ((index = fr.read(buffer)) != -1){
                        fw.write(buffer, 0, index);
                        progress += index;
                        emitter.onNext(progress);
                        fw.flush();
                    }
                    emitter.onComplete();
                }
            }
        });
    }
}
