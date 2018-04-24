package com.soap.networkmaster;

public interface INetworkStateView {
    /**
     * 显示需要开启开机自动启动
     */
    void showEnableAutoStartDialog();

    /**
     * 显示加载进度条
     */
    void showLoadProgress();

    /**
     * 隐藏加载进度条
     */
    void dismissLoadProgress();
}
