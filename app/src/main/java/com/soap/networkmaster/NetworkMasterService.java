package com.soap.networkmaster;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class NetworkMasterService extends IntentService {
    public static final String TAG = "NetworkMasterService";
    private List<AppInfo> mAppInfos;
    private INetworkStateModel mINetworkStateModel;

    public NetworkMasterService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mINetworkStateModel = new NetworkStateModelImpl();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (null != intent) {
            String action = intent.getAction();
            LogUtils.d(TAG, "[onStartCommand] action=" + action);
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case Intent.ACTION_BOOT_COMPLETED:
                        mINetworkStateModel.initFirewall(this);
                        mINetworkStateModel.saveFireWallInitFlag(this, true);
                        break;
                    case Intent.ACTION_SHUTDOWN:
                        mINetworkStateModel.saveFireWallInitFlag(this, false);
                        break;
                    case IntentUtils.ACTION_INIT_FIREWALL:
                        mINetworkStateModel.initFirewall(this);
                        mINetworkStateModel.saveFireWallInitFlag(this, true);
                        break;
                    case Intent.ACTION_PACKAGE_REMOVED:
                        String pkg = intent.getStringExtra(IntentUtils.EXTRA_PKG);
                        int uid = intent.getIntExtra(IntentUtils.EXTRA_UID, -1);
                        mINetworkStateModel.removeNetworkState(this, pkg, uid);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
