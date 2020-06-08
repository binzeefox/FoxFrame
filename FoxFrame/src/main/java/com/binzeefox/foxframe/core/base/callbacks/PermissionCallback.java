package com.binzeefox.foxframe.core.base.callbacks;

import java.util.List;

/**
 * 权限获取回调
 * @author binze
 * 2019/12/10 12:13
 */
public interface PermissionCallback {
    void callback(List<String> failedList);
}
