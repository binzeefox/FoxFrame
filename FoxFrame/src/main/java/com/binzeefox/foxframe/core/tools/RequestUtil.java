package com.binzeefox.foxframe.core.tools;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;


/**
 * 请求工具类
 *
 * @author 狐彻
 * 2020/09/08 12:00
 */
public class RequestUtil {
    private static final String TAG = "RequestUtil";
    private static final String FRAGMENT_TAG = "request_fragment_tag";   //fragment tag
    private final InnerFragment mFragment;  //主业务Fragment
    private final Intent mSAFIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT); //SAF Intent

    /**
     * 私有化构造器
     *
     * @param manager 碎片管理器
     * @author 狐彻 2020/09/08 11:16
     */
    private RequestUtil(final FragmentManager manager) {
        InnerFragment fragment = (InnerFragment) manager.findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            mFragment = fragment;
            return;
        }

        // 若为空，说明第一次添加Fragment
        mFragment = new InnerFragment();
        manager.beginTransaction()
                .add(mFragment, FRAGMENT_TAG)
                .commitNow();

        mSAFIntent.addCategory(Intent.CATEGORY_OPENABLE);
        mSAFIntent.setType("*/*");
    }

    ///////////////////////////////////////////////////////////////////////////
    // 静态获取
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 通过activity获取
     *
     * @author 狐彻 2020/09/08 11:19
     */
    public static RequestUtil with(final AppCompatActivity activity) {
        return new RequestUtil(activity.getSupportFragmentManager());
    }

    /**
     * 通过fragment获取
     *
     * @author 狐彻 2020/09/08 11:19
     */
    public static RequestUtil with(final Fragment fragment) {
        return new RequestUtil(fragment.getChildFragmentManager());
    }

    public static RequestUtil with(final FragmentManager manager){
        return new RequestUtil(manager);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 参数方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 直接请求跳转Intent
     *
     * @author 狐彻 2020/09/08 12:15
     */
    public Observable<Result> intentRequest(Intent intent, int requestCode) {
        return intentRequest(intent, null, requestCode);
    }

    /**
     * 直接请求跳转Intent
     *
     * @author 狐彻 2020/09/08 12:15
     */
    public Observable<Result> intentRequest(final Intent intent, final Bundle options, final int requestCode) {
        return Observable.create(new ObservableOnSubscribe<Result>() {
            @Override
            public void subscribe(ObservableEmitter<Result> emitter) throws Exception {
                mFragment._emitter = emitter;
                mFragment.request(intent, options, requestCode);
            }
        });
    }

    /**
     * 设置MIME 类型，默认全部
     *
     * @author 狐彻 2020/09/08 12:27
     */
    public RequestUtil type(String type) {
        mSAFIntent.setType(type);
        return this;
    }

    /**
     * 是否过滤可开启文件，默认开启
     *
     * @author 狐彻 2020/09/08 12:29
     */
    public RequestUtil openable(boolean openable) {
        if (openable)
            mSAFIntent.addCategory(Intent.CATEGORY_OPENABLE);
        else
            mSAFIntent.removeCategory(Intent.CATEGORY_OPENABLE);
        return this;
    }

    /**
     * 开始请求
     *
     * @author 狐彻 2020/09/08 12:33
     */
    public Observable<Result> request(int requestCode){
        return intentRequest(mSAFIntent, null, requestCode);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 内部业务Fragment
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 内部无视图Fragment
     *
     * @author 狐彻 2020/09/08 12:09
     */
    public static class InnerFragment extends Fragment {
        private ObservableEmitter<Result> _emitter;  //发射器

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        /**
         * 开始请求
         *
         * @param intent      跳转Intent
         * @param options     携带参数
         * @param requestCode 请求码
         * @author 狐彻 2020/09/08 12:19
         */
        private void request(Intent intent, Bundle options, int requestCode) {
            if (options == null)
                startActivityForResult(intent, requestCode);
            else
                startActivityForResult(intent, requestCode, options);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            _emitter.onNext(new Result(data, resultCode, requestCode));
            _emitter.onComplete();
        }
    }

    /**
     * 请求结果包装类
     *
     * @author binze 2019/11/29 15:38
     */
    public static class Result {
        private Intent data; //返回数据
        private int resultCode;  //返回码
        private int requestCode; //请求码

        private Result(Intent data, int resultCode, int requestCode) {
            this.data = data;
            this.resultCode = resultCode;
            this.requestCode = requestCode;
        }

        public Intent getData() {
            return data;
        }

        public int getResultCode() {
            return resultCode;
        }

        public int getRequestCode() {
            return requestCode;
        }
    }
}
