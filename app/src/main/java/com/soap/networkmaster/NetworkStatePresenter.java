package com.soap.networkmaster;

import android.content.Context;
import android.os.AsyncTask;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;

import java.util.List;

public class NetworkStatePresenter {
    private final static String TAG = "NetworkStatePresenter";

    private INetworkStateModel mModel;
    private INetworkStateView mView;
    private INetworkManagementService mNetworkService;

    public NetworkStatePresenter(INetworkStateView view) {
        mModel = new NetworkStateModelImpl();
        mView = view;
        mNetworkService = INetworkManagementService.Stub.asInterface(
                ServiceManager.getService("network_management"/*Context.NETWORKMANAGEMENT_SERVICE*/));
    }

    public void checkFireWallInitState(Context context) {
        if (!mModel.readFireWallInitFlag(context)) {
            mView.showEnableAutoStartDialog();
        }
    }

    public void setNetworkState(Context context, String pkgName, int uid, String type, boolean state) {
        try {
            /*
             * 如果是要禁止掉某个APP访问网络的话，
             * 应该是要下allow(true),而不是下deny(false)，
             * deny(false)是不禁止，allow(true)是允许禁止
             */
            mNetworkService.setFirewallUidChainRule(uid, Utils.getNetworkIdByType(type), !state);
        } catch (RemoteException e) {
            LogUtils.e(TAG, "[setNetworkState]  connected to NetworkManagementService failed");
        }
        mModel.saveNetworkState(context, pkgName, uid, type, state);
    }

    public void loadAllAppNetworkState(Context context, List<AppInfo> appInfos) {
        new NetworkStateLoadTask().execute(context, appInfos, mModel, mView);
    }

    private static class NetworkStateLoadTask extends AsyncTask<Object, Object, INetworkStateView> {

        @Override
        protected INetworkStateView doInBackground(Object[] views) {
            if (views != null && views.length > 3) {
                Context context = (Context)views[0];
                List<AppInfo> appInfos = (List<AppInfo>)views[1];
                INetworkStateModel model = (INetworkStateModel)views[2];
                INetworkStateView view = (INetworkStateView)views[3];
                view.showLoadProgress();
                for (AppInfo appInfo : appInfos) {
                    appInfo.setMobileState(model.readNetworkState(context, appInfo.getPackageName(),
                            appInfo.getUid(), Utils.TYPE_MOBILE));
                    appInfo.setWifiState(model.readNetworkState(context, appInfo.getPackageName(),
                            appInfo.getUid(), Utils.TYPE_WIFI));
                }
                return view;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(INetworkStateView view) {
            if (view != null) {
                view.dismissLoadProgress();
            }
        }
    }
}
