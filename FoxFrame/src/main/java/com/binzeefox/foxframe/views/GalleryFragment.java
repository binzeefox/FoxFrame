package com.binzeefox.foxframe.views;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.binzeefox.foxframe.R;
import com.binzeefox.foxframe.core.FoxCore;
import com.binzeefox.foxframe.core.base.BaseLazyFragment;
import com.binzeefox.foxframe.tools.dev.LogUtil;
import com.binzeefox.foxframe.tools.dev.ThreadUtil;
import com.binzeefox.foxframe.tools.image.ImageUtil;
import com.binzeefox.foxframe.tools.image.RxMediaPicker;
import com.binzeefox.foxframe.tools.resource.UriUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static android.app.Activity.RESULT_OK;

/**
 * 相册Fragment
 *
 * @author 狐彻
 * 2020/09/23 15:26
 */
public class GalleryFragment extends BaseLazyFragment {
    public static final String PARAMS_MEDIA_LIST = "params_media_list";
    public static final String PARAMS_SHOW_VIDEO = "params_show_video";
    public static final String PARAMS_SHOW_ADD = "params_show_add";
    public static final String PARAMS_SHOW_KNOWN = "params_show_known";

    private static final int TYPE_ADD = 0;
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_VIDEO = 2;
    private static final int TYPE_UNKNOWN = -1;

    private static final String TAG = "GalleryFragment";
    private RecyclerView mInnerListView;    //内部实现的RecyclerView
    private boolean mShowVideo = false; //是否显示视频
    private boolean mShowAdd = true;    //是否显示新增按钮
    private boolean mShowUnknown = false;   //是否显示无法显示项

    private GalleryCallback mCallback;  //操作回调
    private ListAdapter mAdapter;

    private final List<Uri> mMediaList = new ArrayList<>();  //所有要展示的内容

    public static GalleryFragment newInstance(@NonNull ArrayList<Uri> mediaList, boolean showVideo, boolean showAdd, boolean showUnknown) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(PARAMS_MEDIA_LIST, mediaList);
        args.putBoolean(PARAMS_SHOW_VIDEO, showVideo);
        args.putBoolean(PARAMS_SHOW_ADD, showAdd);
        args.putBoolean(PARAMS_SHOW_KNOWN, showUnknown);
        GalleryFragment fragment = new GalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View onSetLayoutView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        mInnerListView = new RecyclerView(inflater.getContext());
        mInnerListView.setLayoutParams(new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        );
        return mInnerListView;
    }

    @Override
    protected void create(View view, Bundle bundle) {
        if (getArguments() == null) return;
        List<Uri> uriList = getArguments().getParcelableArrayList(PARAMS_MEDIA_LIST);
        mShowVideo = getArguments().getBoolean(PARAMS_SHOW_VIDEO);
        mShowAdd = getArguments().getBoolean(PARAMS_SHOW_ADD);
        mShowUnknown = getArguments().getBoolean(PARAMS_SHOW_KNOWN);
        if (uriList != null) mMediaList.addAll(uriList);
        mInnerListView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new ListAdapter();
        GridLayoutManager layoutManager = new GridLayoutManager
                (view.getContext(), 3, RecyclerView.VERTICAL, false);

        mInnerListView.setLayoutManager(layoutManager);
    }

    /**
     * 设置回调
     *
     * @author 狐彻 2020/09/23 16:32
     */
    public void setGalleryCallback(GalleryCallback callback) {
        mCallback = callback;
    }

    /**
     * 设置子项加载、事件监听。调用该方法将使GalleryCallback无效化
     *
     * @author 狐彻 2020/09/24 12:03
     */
    public void setItemViewCallback(ItemViewCallback callback){
        mAdapter = new ListAdapter();
        mAdapter.callback = callback;
        mInnerListView.setAdapter(mAdapter);
        mInnerListView.invalidate();
    }

    @Override
    protected void onLoad() {
        //过滤数据源

        //若全部显示，则没必要过滤
        if (mShowVideo && mShowUnknown) return;

        final List<Uri> uriList = new ArrayList<>();
        mMediaList.forEach(new Consumer<Uri>() {
            @Override
            public void accept(Uri uri) {
                if (!mShowVideo && GalleryFragment.this.checkType(uri) == TYPE_VIDEO)
                    return;
                if (!mShowUnknown && GalleryFragment.this.checkType(uri) == TYPE_UNKNOWN)
                    return;
                uriList.add(uri);
            }
        });

        mMediaList.clear();
        mMediaList.addAll(uriList);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mInnerListView.setAdapter(mAdapter);
            }
        });
    }

    /**
     * TODO 点击了增加按钮
     *
     * @author 狐彻 2020/09/23 16:35
     */
    private void onPressAdd(View view) {
        //显示底部弹窗
        CardView container = new CardView(view.getContext());
        ViewGroup.LayoutParams containerParams = new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        container.setLayoutParams(containerParams);

        ListView listView = new ListView(view.getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        List<String> items = new ArrayList<>();
        items.add("相册");
        items.add("拍照");
        if (mShowVideo) items.add("摄像");

        listView.setLayoutParams(params);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (view.getContext(), android.R.layout.simple_list_item_1, items) {

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                //列表文字居中
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setGravity(Gravity.CENTER);
                return tv;
            }
        };
        listView.setAdapter(adapter);

        container.addView(listView);

        final BottomSheetDialog dialog = new BottomSheetDialog(view.getContext());
        dialog.setContentView(container);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view1, int position, long id) {
                switch (position) {
                    case 0:
                        GalleryFragment.this.openSystemGallery();
                        break;
                    case 1:
                        GalleryFragment.this.openSystemCamera();
                        break;
                    case 2:
                        GalleryFragment.this.openSystemVideoRecorder();
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 开启系统相册
     *
     * @author 狐彻 2020/09/24 10:10
     */
    private void openSystemGallery() {
        RxMediaPicker.with(this)
                .openGallery(0x00)
                .subscribe(new MediaObserver());
    }

    /**
     * 开启系统相机
     *
     * @author 狐彻 2020/09/24 10:11
     */
    private void openSystemCamera() {
        File file= FoxCore.getApplication().getCacheDir();
        file = new File(file.getPath() + "/temp_" + System.currentTimeMillis() + ".jpeg");
        Uri tempUri = UriUtil.get().fileToUri(file, FoxCore.getAuthority());

        RxMediaPicker.with(this)
                .openCamera(0x01, tempUri)
                .subscribe(new MediaObserver());
    }

    /**
     * 开启系统录像
     *
     * @author 狐彻 2020/09/24 10:11
     */
    private void openSystemVideoRecorder() {
        File file= FoxCore.getApplication().getCacheDir();
        file = new File(file.getPath() + "/temp_" + System.currentTimeMillis() + ".mp4");
        Uri tempUri = UriUtil.get().fileToUri(file, FoxCore.getAuthority());
        RxMediaPicker.with(this)
                .openVideoRecorder(0x02, tempUri)
                .subscribe(new MediaObserver());
    }

    /**
     * 检查格式
     *
     * @author 狐彻 2020/09/23 16:52
     */
    private int checkType(Uri uri) {
        String type = UriUtil.get().getUriMimeType(uri);
        if (type != null && type.startsWith("image"))
            return TYPE_IMAGE;
        if (type != null && type.startsWith("video"))
            return TYPE_VIDEO;
        return TYPE_UNKNOWN;
    }


    ///////////////////////////////////////////////////////////////////////////
    // 适配器
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 列表适配器
     *
     * @author 狐彻 2020/09/23 16:53
     */
    private class ListAdapter extends RecyclerView.Adapter<ViewHolder> {
        @NonNull
        private ItemViewCallback callback = new ItemViewCallback() {
            @Override
            public View createAddButton(@NonNull ViewGroup parent) {
                return LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_gallery_fragment_add, parent, false);
            }

            @Override
            public View createImageItem(@NonNull ViewGroup parent) {
                return LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_gallery_fragment_image, parent, false);
            }

            @Override
            public View createVideoItem(@NonNull ViewGroup parent) {
                return LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_gallery_fragment_video, parent, false);
            }

            @Override
            public View createUnknownItem(@NonNull ViewGroup parent) {
                return LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_gallery_fragment_known, parent, false);
            }

            @Override
            public void onSetUpAddButton(@NonNull ViewGroup container) {
                container.findViewById(R.id.press_archer)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onPressAdd(v);
                            }
                        });
            }

            @Override
            public void onSetUpImageItem(@NonNull final Uri uri, ViewGroup container) {
                final ImageView view = container.findViewById(R.id.item_fragment_gallery_view);
                view.setImageResource(R.drawable.ic_baseline_image_66);
                ThreadUtil.get().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Drawable d = ImageUtil.get().decode(uri).decodeDrawable();
                            setImageMainThread(d, view);
                        } catch (IOException e) {
                            LogUtil.e(TAG, "run: 加载图片失败", e);
                            setImageMainThread(null, view);
                        }
                    }
                });
                container.findViewById(R.id.press_archer).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCallback != null)
                            mCallback.onPressItem(uri, UriUtil.get().getUriMimeType(uri));
                    }
                });
            }

            @Override
            public void onSetUpVideoItem(@NonNull final Uri uri, ViewGroup container) {
                final ImageView view = container.findViewById(R.id.item_fragment_gallery_view);
                view.setImageResource(R.drawable.ic_baseline_image_66);
                ThreadUtil.get().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Drawable d = ImageUtil.get().decode(uri).decodeDrawable();
                            setImageMainThread(d, view);
                        } catch (IOException e) {
                            LogUtil.e(TAG, "run: 加载图片失败", e);
                            setImageMainThread(null, view);
                        }
                    }
                });

                container.findViewById(R.id.press_archer).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCallback != null)
                            mCallback.onPressItem(uri, UriUtil.get().getUriMimeType(uri));
                    }
                });
            }

            @Override
            public void onSetUpUnknownItem(@NonNull final Uri uri, ViewGroup container) {
                ImageView view = container.findViewById(R.id.item_fragment_gallery_view);
                view.setImageResource(R.drawable.ic_baseline_broken_image_66);
                container.findViewById(R.id.press_archer).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCallback != null)
                            mCallback.onPressItem(uri, UriUtil.get().getUriMimeType(uri));
                    }
                });
            }

            /**
             * 主线程设置图片
             *
             * @author 狐彻 2020/09/24 11:52
             */
            private void setImageMainThread(final Drawable d, final ImageView view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (d == null) view.setImageResource(R.drawable.ic_baseline_broken_image_66);
                        else view.setImageDrawable(d);
                    }
                });
            }
        };

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_ADD)
                return new ViewHolder(callback.createAddButton(parent));
            if (viewType == TYPE_IMAGE)
                return new ViewHolder(callback.createImageItem(parent));
            if (viewType == TYPE_VIDEO)
                return new ViewHolder(callback.createVideoItem(parent));
            return new ViewHolder(callback.createUnknownItem(parent));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int _position = mShowAdd ? position - 1 : position;
            switch (holder.getItemViewType()) {
                case TYPE_ADD:
                    callback.onSetUpAddButton((ViewGroup) holder.itemView);
                    break;
                case TYPE_IMAGE:
                    callback.onSetUpImageItem(mMediaList.get(_position), (ViewGroup) holder.itemView);
                    break;
                case TYPE_VIDEO:
                    callback.onSetUpVideoItem(mMediaList.get(_position), (ViewGroup) holder.itemView);
                default:
                    callback.onSetUpUnknownItem(mMediaList.get(_position), (ViewGroup) holder.itemView);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mShowAdd && position == 0) return TYPE_ADD;
            int _position = mShowAdd ? position - 1 : position;
            return checkType(mMediaList.get(_position));
        }

        @Override
        public int getItemCount() {
            return mShowAdd ? mMediaList.size() + 1 : mMediaList.size();
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // 内部类
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 相册事件回调
     *
     * @author 狐彻 2020/09/23 16:53
     */
    public interface GalleryCallback {

        void onPressItem(Uri uri, String type);

        void onAddMedia(Uri uri, String type);
    }

    /**
     * 加载布局回调
     *
     * @author 狐彻 2020/09/23 21:13
     */
    public interface ItemViewCallback {

        /**
         * 创建新增按钮
         *
         * @author 狐彻 2020/09/23 21:14
         */
        View createAddButton(@NonNull ViewGroup parent);

        /**
         * 创建图像子项
         *
         * @author 狐彻 2020/09/23 21:17
         */
        View createImageItem(@NonNull ViewGroup parent);

        /**
         * 创建视频子项
         *
         * @author 狐彻 2020/09/23 21:17
         */
        View createVideoItem(@NonNull ViewGroup parent);

        /**
         * 创建未知类型框
         *
         * @author 狐彻 2020/09/23 22:04
         */
        View createUnknownItem(@NonNull ViewGroup parent);

        /**
         * 设置新增按钮布局
         *
         * @author 狐彻 2020/09/23 21:19
         */
        void onSetUpAddButton(@NonNull ViewGroup container);

        /**
         * 设置图片布局
         *
         * @author 狐彻 2020/09/23 21:22
         */
        void onSetUpImageItem(@NonNull Uri uri, ViewGroup container);

        /**
         * 设置视频布局
         *
         * @author 狐彻 2020/09/23 21:21
         */
        void onSetUpVideoItem(@NonNull Uri uri, ViewGroup container);

        /**
         * 设置未知类型框
         *
         * @author 狐彻 2020/09/23 22:04
         */
        void onSetUpUnknownItem(@NonNull Uri uri, ViewGroup container);
    }

    /**
     * 视图容器
     *
     * @author 狐彻 2020/09/23 22:39
     */
    private static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /**
     * 实现的系统获取图像Observer
     *
     * @author 狐彻 2020/09/24 10:29
     */
    private class MediaObserver implements Observer<RxMediaPicker.Result> {

        @Override
        public void onSubscribe(Disposable d) {
            dContainer.add(d);
        }

        @Override
        public void onNext(RxMediaPicker.Result result) {
            if (result.getResultCode() != RESULT_OK) {
                if (mCallback != null)
                    mCallback.onAddMedia(null, "error");
                LogUtil.w(TAG, "onNext: 获取图片失败，result_code = " + result.getResultCode());
                return;
            }
            Uri uri = result.getMediaUri();
            if (uri == null) uri = result.getData().getData();
            if (mCallback != null)
                mCallback.onAddMedia(uri, UriUtil.get().getUriMimeType(uri));
            mMediaList.add(0, uri);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onError(Throwable e) {
            LogUtil.e(TAG, "onError: 获取媒体失败", e);
        }

        @Override
        public void onComplete() {

        }
    }
}
