package com.binzeefox.androidtemplete;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.binzeefox.foxframe.core.base.FoxActivity;
import com.binzeefox.foxframe.views.GalleryFragment;

/**
 * 相册页
 *
 * @author 狐彻
 * 2020/10/15 14:52
 */
public class GalleryActivity extends FoxActivity {

    @Override
    protected int onSetLayoutResource() {
        return R.layout.activity_gallery;
    }

    @Override
    protected void create(Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putBoolean(GalleryFragment.PARAMS_SHOW_ADD, true);
        fragment.setArguments(args);
        fragment.setGalleryCallback(new GalleryFragment.GalleryCallback() {
            @Override
            public void onPressItem(Uri uri, String type) {

            }

            @Override
            public void onAddMedia(Uri uri, String type) {

            }
        });
        ft.replace(R.id.fragment_container, fragment, "fragment_gallery");
        ft.commitNow();
    }
}
