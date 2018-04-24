package com.soap.networkmaster;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity implements INetworkStateView {
    public static final String TAG = "NetworkMaster";

    private Switch mWifiAll;
    private Switch mMobileAll;
    private ListView mListView;
    private List<AppInfo> mAppInfos;
    private AppAdapter mAppAdapter;
    private int mWifiOnCount;
    private int mMobileOnCount;
    private ProgressDialog mProgressDialog;
    private NetworkStatePresenter mNetworkStatePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
         * 注意: FW设置会在重启后失效，应用可能被杀毒软件禁止开机启动，或者某些情况下收不到开机广播 启动Activity时，需先初始化FW
         */
        mNetworkStatePresenter = new NetworkStatePresenter(this);
        mNetworkStatePresenter.checkFireWallInitState(this);

        mWifiAll = (Switch) findViewById(R.id.wifi_all);
        mMobileAll = (Switch) findViewById(R.id.mobile_all);
        mListView = (ListView) findViewById(R.id.net_control_listview);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.load_title);
        mProgressDialog.setMessage(getString(R.string.load_msg));
        mAppInfos = Utils.getAppInfos(this, Utils.FILTER_ALL_APP);
        mAppAdapter = new AppAdapter(mAppInfos, this);
        mListView.setAdapter(mAppAdapter);
        mWifiAll.setOnClickListener(new WifiAllOnClickListener());
        mMobileAll.setOnClickListener(new MobileAllOnClickListener());
        mNetworkStatePresenter.loadAllAppNetworkState(this, mAppInfos);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNetworkStatePresenter = null;
    }

    @Override
    public void showEnableAutoStartDialog() {
        new AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(R.string.start_at_boot_not_allowed)
                .create().show();
        LogUtils.d(TAG, "did not receive ACTION_BOOT_COMPLETED, start NetworkMasterService to init fw");
        Intent initFwIntent = new Intent();
        initFwIntent.setAction(IntentUtils.ACTION_INIT_FIREWALL);
        initFwIntent.setClass(this, NetworkMasterService.class);
        startService(initFwIntent);
    }

    @Override
    public void showLoadProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()
                        && !MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed()) {
                    mProgressDialog.show();
                }
            }
        });
    }

    @Override
    public void dismissLoadProgress() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()
                        && !MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed()) {
                    mProgressDialog.dismiss();
                    mAppAdapter.notifyDataSetChanged();
                    getWifiAndMobileCount();
                    updateAllState(Utils.TYPE_WIFI_ID);
                    updateAllState(Utils.TYPE_MOBILE_ID);
                }
                getWifiAndMobileCount();
                updateAllState(Utils.TYPE_WIFI_ID);
                updateAllState(Utils.TYPE_MOBILE_ID);
            }
        });
    }

    private class AppAdapter extends BaseAdapter {
        List<AppInfo> appInfos;
        LayoutInflater layoutInflater;

        AppAdapter(List<AppInfo> list, Context context) {
            this.appInfos = list;
            this.layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if (appInfos != null) {
                return appInfos.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            if (appInfos != null) {
                return appInfos.get(position);
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.app_list_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final AppInfo item = (AppInfo) getItem(position);
            LogUtils.d(TAG, "getView() position=" + position + "; pkgName=" + item.getPackageName() + "; uid="
                    + item.getUid() + "; wifi=" + item.getWifiState() + "; mobile=" + item.getMobileState());
            holder.icon.setImageDrawable(item.getAppIcon());
            holder.label.setText(item.getAppLabel());
            holder.wifi.setChecked(item.getWifiState());
            holder.mobile.setChecked(item.getMobileState());

            holder.wifi.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    String pkgName = item.getPackageName();
                    boolean isChecked = ((Switch) view).isChecked();
                    LogUtils.d(TAG, "Wifi onCheckedChanged() packageName=" + pkgName + "; isChecked=" + isChecked);
                    item.setWifiState(isChecked);
                    int uid = item.getUid();
                    if (isChecked) {
                        if (mWifiOnCount < mAppInfos.size())
                            mWifiOnCount++;
                    } else {
                        if (mWifiOnCount > 0)
                            mWifiOnCount--;
                    }
                    LogUtils.d(TAG, "Wifi onCheckedChanged() mWifiOnCount=" + mWifiOnCount);
                    updateAllState(Utils.TYPE_WIFI_ID);
                    mNetworkStatePresenter.setNetworkState(MainActivity.this, pkgName, uid, Utils.TYPE_WIFI, isChecked);
                }
            });

            holder.mobile.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    String pkgName = item.getPackageName();
                    boolean isChecked = ((Switch) view).isChecked();
                    LogUtils.d(TAG, "Mobile onCheckedChanged() packageName=" + pkgName + "; isChecked=" + isChecked);
                    item.setMobileState(isChecked);
                    int uid = item.getUid();
                    if (isChecked) {
                        if (mMobileOnCount < mAppInfos.size())
                            mMobileOnCount++;
                    } else {
                        if (mMobileOnCount > 0)
                            mMobileOnCount--;
                    }
                    LogUtils.d(TAG, "Mobile onCheckedChanged() mMobileOnCount=" + mMobileOnCount);
                    updateAllState(Utils.TYPE_MOBILE_ID);
                    mNetworkStatePresenter.setNetworkState(MainActivity.this, pkgName, uid, Utils.TYPE_MOBILE, isChecked);
                }
            });
            return convertView;
        }
    }

    private static final class ViewHolder {
        public ImageView icon;
        public TextView label;
        public Switch wifi;
        public Switch mobile;

        ViewHolder(View convertView) {
            this.icon = (ImageView) convertView.findViewById(R.id.app_icon);
            this.label = (TextView) convertView.findViewById(R.id.app_label);
            this.wifi = (Switch) convertView.findViewById(R.id.wifi);
            this.mobile = (Switch) convertView.findViewById(R.id.mobile);
        }
    }

    /*
     * 全选wifi监听器
     */
    private class WifiAllOnClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            boolean isChecked = ((Switch) view).isChecked();
            LogUtils.d(TAG, "WifiAll onCheckedChanged() isChecked=" + isChecked);
            if (isChecked) {
                for (AppInfo info : mAppInfos) {
                    if (!info.getWifiState()) {
                        info.setWifiState(true);
                        mNetworkStatePresenter.setNetworkState(MainActivity.this, info.getPackageName(),
                                info.getUid(), Utils.TYPE_WIFI, true);
                    }
                }
                mWifiOnCount = mAppInfos.size();
            } else {
                LogUtils.d(TAG, "WifiAll onCheckedChanged() mWifiOnCount=" + mWifiOnCount);
                for (AppInfo info : mAppInfos) {
                    if (info.getWifiState()) {
                        info.setWifiState(false);
                        mNetworkStatePresenter.setNetworkState(MainActivity.this, info.getPackageName(),
                                info.getUid(), Utils.TYPE_WIFI, false);
                    }
                }
                mWifiOnCount = 0;
            }
            mAppAdapter.notifyDataSetChanged();
        }
    }

    /*
     * 全选移动数据监听器
     */
    private class MobileAllOnClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            boolean isChecked = ((Switch) view).isChecked();
            LogUtils.d(TAG, "MobileAll onCheckedChanged() isChecked=" + isChecked);
            if (isChecked) {
                for (AppInfo info : mAppInfos) {
                    if (!info.getMobileState()) {
                        info.setMobileState(true);
                        mNetworkStatePresenter.setNetworkState(MainActivity.this, info.getPackageName(),
                                info.getUid(), Utils.TYPE_MOBILE, true);
                    }
                }
                mMobileOnCount = mAppInfos.size();
            } else {
                LogUtils.d(TAG, "MobileAll onCheckedChanged() mMobileOnCount=" + mMobileOnCount);
                for (AppInfo info : mAppInfos) {
                    if (info.getMobileState()) {
                        info.setMobileState(false);
                        mNetworkStatePresenter.setNetworkState(MainActivity.this, info.getPackageName(),
                                info.getUid(), Utils.TYPE_MOBILE, false);
                    }
                }
                mMobileOnCount = 0;
            }
            mAppAdapter.notifyDataSetChanged();
        }
    }

    private void updateAllState(int type) {
        if (type == Utils.TYPE_WIFI_ID) {
            if (mWifiOnCount == mAppInfos.size()) {
                mWifiAll.setChecked(true);
            } else {
                mWifiAll.setChecked(false);
            }
        } else if (type == Utils.TYPE_MOBILE_ID) {
            if (mMobileOnCount == mAppInfos.size()) {
                mMobileAll.setChecked(true);
            } else {
                mMobileAll.setChecked(false);
            }
        }
    }

    private void getWifiAndMobileCount() {
        for (AppInfo info : mAppInfos) {
            if (info.getWifiState() && mWifiOnCount < mAppInfos.size()) {
                mWifiOnCount++;
            }

            if (info.getMobileState() && mMobileOnCount < mAppInfos.size()) {
                mMobileOnCount++;
            }
        }
        LogUtils.d(TAG, "getWifiAndMobileCount()  mWifiOnCount=" + mWifiOnCount + "; mMobileOnCount="
                + mMobileOnCount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
