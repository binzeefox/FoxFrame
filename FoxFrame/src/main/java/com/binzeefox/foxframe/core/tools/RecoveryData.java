package com.binzeefox.foxframe.core.tools;

/**
 * 在GC回收时自动本地化，get时自动赋值的数据类
 *
 * @author 狐彻
 * 2020/09/12 11:33
 */
public abstract class RecoveryData {
    
    public RecoveryData(){
        recoverData();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        localizeData();
    }

    /**
     * 将数据本地化
     * 
     * @author 狐彻 2020/09/12 11:36
     */
    protected abstract void localizeData();

    /**
     * 将本地化的数据恢复
     * 
     * @author 狐彻 2020/09/12 11:35
     */
    protected abstract void recoverData();
}
