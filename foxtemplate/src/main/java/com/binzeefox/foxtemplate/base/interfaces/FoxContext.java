package com.binzeefox.foxtemplate.base.interfaces;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.binzeefox.foxtemplate.base.FoxActivity;
import com.binzeefox.foxtemplate.base.FoxFragment;

/**
 * 自制的Context
 * 用来统一{@link FoxActivity} 和 {@link FoxFragment}
 * 暂时没想好要不要用。。。
 * 总之先用上了
 * <p>
 * {@link #navigate(Class)} 跳转
 * {@link #navigate(Class, Bundle)} 带参数跳转
 * {@link #navigateForResult(Class, int)}   带返回跳转
 * {@link #navigateForResult(Class, Bundle, int)}   带返回带参数跳转
 * {@link #navigateForResult(Intent, int)}   隐式带返回跳转
 *
 * @author binze
 * 2019/11/7 15:10
 */
public interface FoxContext {
    void navigate(Class<? extends Activity> target);

    void navigate(Class<? extends Activity> target, Bundle params);

    void navigateForResult(Class<? extends Activity> target, int requestCode);

    void navigateForResult(Class<? extends Activity> target, Bundle params, int requestCode);

    void navigateForResult(Intent intent, int requestCode);
}
