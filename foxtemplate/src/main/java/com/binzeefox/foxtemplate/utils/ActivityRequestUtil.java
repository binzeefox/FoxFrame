package com.binzeefox.foxtemplate.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.binzeefox.foxtemplate.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.disposables.DisposableContainer;

/**
 * 用类似RxAndroid的方式接管{@link Activity#startActivityForResult(Intent, int)}
 *
 * @author binze
 * 2019/11/29 15:31
 */
public class ActivityRequestUtil {
    private static final String TAG = "ActivityRequestUtil";
    private static final int REQUEST_CODE = R.id.code_activity_request;
    private static final String REQUEST_TAG = "REQUEST_RESULT_FRAGMENT";
    private static RequestFragment mFragment;   //业务承载者
    private Intent mIntent; //请求Intent
    private Bundle mOptions;    //请求Options

    /**
     * 静态初始化
     *
     * @param intent    请求Intent
     * @author binze 2019/11/29 15:58
     */
    public static ActivityRequestUtil init(Intent intent) {
        return new ActivityRequestUtil(intent, null);
    }

    /**
     * 静态初始化
     *
     * @param intent    请求Intent
     * @param options   请求参数
     * @author binze 2019/11/29 15:58
     */
    public static ActivityRequestUtil init(Intent intent, Bundle options) {
        return new ActivityRequestUtil(intent, options);
    }

    /**
     * 私有化构造器
     *
     * @param intent    请求Intent
     * @param options   请求参数
     * @author binze 2019/11/29 15:59
     */
    private ActivityRequestUtil(Intent intent, Bundle options) {
        mIntent = intent;
        mOptions = options;
    }

    /**
     * Activity请求
     * @author binze 2019/11/29 15:59
     */
    public Observable<Result> request(AppCompatActivity activity) {
        getRequestFragment(activity.getSupportFragmentManager());
        return Observable.create((ObservableOnSubscribe<Result>) emitter -> {
            mFragment.emitter = emitter;
            if (mOptions != null)
                mFragment.startRequest(mIntent, mOptions);
            else
                mFragment.startRequest(mIntent);
        }).compose(RxUtil.setThread());
    }

    /**
     * Fragment请求
     * @author binze 2019/11/29 15:59
     */
    public Observable<Result> request(Fragment fragment) {
        getRequestFragment(fragment.getChildFragmentManager());
        return Observable.create((ObservableOnSubscribe<Result>) emitter -> {
            mFragment.emitter = emitter;
            if (mOptions != null)
                mFragment.startRequest(mIntent, mOptions);
            else
                mFragment.startRequest(mIntent);
        }).compose(RxUtil.setThread());
    }

    /**
     * 获取请求Fragment，若没有则创建
     *
     * @author binze 2019/11/29 15:52
     */
    private void getRequestFragment(final FragmentManager manager) {
        RequestFragment fragment = (RequestFragment) manager.findFragmentByTag(REQUEST_TAG);
        if (fragment != null) {
            mFragment = fragment;
            return;
        }
        fragment = new RequestFragment();
        manager
                .beginTransaction()
                .add(fragment, REQUEST_TAG)
                .commitNow();
        mFragment = fragment;
    }

    /**
     * 处理请求的Fragment
     *
     * @author binze 2019/11/29 15:38
     */
    public static class RequestFragment extends Fragment {
        private ObservableEmitter<Result> emitter;  //发射器

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        /**
         * 开始请求
         *
         * @author binze 2019/11/29 15:41
         */
        private void startRequest(Intent intent) {
            startActivityForResult(intent, REQUEST_CODE);
        }

        /**
         * 开始请求
         *
         * @author binze 2019/11/29 15:42
         */
        private void startRequest(Intent intent, Bundle options) {
            startActivityForResult(intent, REQUEST_CODE, options);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            emitter.onNext(new Result(data, resultCode, requestCode));
            emitter.onComplete();
        }
    }

    /**
     * 请求结果包装类
     *
     * @author binze 2019/11/29 15:38
     */
    public static class Result {
        public Intent data; //返回数据
        public int resultCode;  //返回码
        public int requestCode; //请求码

        private Result(Intent data, int resultCode, int requestCode) {
            this.data = data;
            this.resultCode = resultCode;
            this.requestCode = requestCode;
        }
    }
}
