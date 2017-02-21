package com.itel.networkcontrol;

import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/*
 * 如果是要禁止掉某个APP访问网络的话，
 * 应该是要下allow(true),而不是下deny(false)，
 * deny(false)是不禁止，allow(true)是允许禁止
 */
public class MainActivity extends Activity {
	public static final String TAG = "NetworkControl";

	private CheckBox mWifiAll;
	private CheckBox mMobileAll;
	private ListView mListView;

	private INetworkManagementService mNetworkService;
	private List<AppInfo> mAppList;
	private NetWorkControlAdapter mAdapter;
	private Utils mUtils;
	private int wifiCheckedCount;
	private int mobileCheckedCount;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setLogo(R.drawable.firewall_title_icon);
		mWifiAll = (CheckBox) findViewById(R.id.wifi_all);
		mMobileAll = (CheckBox) findViewById(R.id.mobile_all);
		mListView = (ListView) findViewById(R.id.net_control_listview);

		mUtils = new Utils(this);
		/*
		 * FW设置会在重启后失效，应用可能被杀毒软件禁止开机启动，或者某些情况下收不到开机广播 启动Activity时，需先初始化FW
		 */
		if (!mUtils.readInitFwFlag()) {
			new AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(R.string.start_at_boot_not_allowed)
					.create().show();

			Log.d(TAG, "did not receive ACTION_BOOT_COMPLETED, start InitService to init fw");
			Intent initFwIntent = new Intent();
			initFwIntent.setAction(InitReceiver.INIT_FW);
			initFwIntent.setClass(this, InitService.class);
			startService(initFwIntent);
		}
		mNetworkService = INetworkManagementService.Stub
				.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
		//mAppList = mUtils.queryFilterAppInfo(Utils.FILTER_THIRD_APP);
		mAppList = mUtils.queryFilterAppInfo(Utils.FILTER_ALL_APP);
		mAdapter = new NetWorkControlAdapter(mAppList, this);
		mListView.setAdapter(mAdapter);

		mWifiAll.setOnClickListener(new WifiAllOnClickListener());
		mMobileAll.setOnClickListener(new MobileAllOnClickListener());
		getWifiAndMobileCount();
		updateAllCheckbox(Utils.TYPE_WIFI_ID);
		updateAllCheckbox(Utils.TYPE_MOBILE_ID);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private class NetWorkControlAdapter extends BaseAdapter {
		List<AppInfo> nList;
		LayoutInflater nInflater;

		public NetWorkControlAdapter(List<AppInfo> list, Context context) {
			this.nList = list;
			this.nInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			if (nList != null) {
				return nList.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			if (nList != null) {
				return nList.get(position);
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
				convertView = nInflater.inflate(R.layout.app_list_item, null);
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.app_icon);
				holder.label = (TextView) convertView.findViewById(R.id.app_label);
				holder.wifi = (CheckBox) convertView.findViewById(R.id.wifi_checkbox);
				holder.mobile = (CheckBox) convertView.findViewById(R.id.mobile_checkbox);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final AppInfo item = (AppInfo) getItem(position);
			Log.d(TAG, "getView() position=" + position + "; pkgName=" + item.getPackageName() + "; uid="
					+ item.getUid() + "; wifi=" + item.getWifiState() + "; mobile=" + item.getMobileState());
			holder.icon.setImageDrawable(item.getAppIcon());
			holder.label.setText(item.getAppLabel());
			holder.wifi.setChecked(item.getWifiState());
			holder.mobile.setChecked(item.getMobileState());

			holder.wifi.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					String pkgName = item.getPackageName();
					boolean isChecked = ((CheckBox) view).isChecked();
					Log.d(TAG, "Wifi onCheckedChanged() packageName=" + pkgName + "; isChecked=" + isChecked);
					item.setWifiState(isChecked);
					int uid = item.getUid();

					try {
						mNetworkService.setFirewallUidChainRule(uid, Utils.TYPE_WIFI_ID, isChecked);
						if (!isChecked) {
							if (wifiCheckedCount > 0)
								wifiCheckedCount--;
						} else {
							if (wifiCheckedCount < mAppList.size())
								wifiCheckedCount++;
						}
						Log.d(TAG, "Wifi onCheckedChanged() wifiCheckedCount=" + wifiCheckedCount);
						updateAllCheckbox(Utils.TYPE_WIFI_ID);
						mUtils.saveData(pkgName, uid, Utils.TYPE_WIFI, isChecked);
					} catch (RemoteException e) {
						Log.e(TAG, "WifiCheckedChangeListener-->  connected to NetworkManagementService failed");
					}

				}
			});

			holder.mobile.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					String pkgName = item.getPackageName();
					boolean isChecked = ((CheckBox) view).isChecked();
					Log.d(TAG, "Mobile onCheckedChanged() packageName=" + pkgName + "; isChecked=" + isChecked);
					item.setMobileState(isChecked);
					int uid = item.getUid();
					try {
						mNetworkService.setFirewallUidChainRule(uid, Utils.TYPE_MOBILE_ID, isChecked);
						if (!isChecked) {
							if (mobileCheckedCount > 0)
								mobileCheckedCount--;
						} else {
							if (mobileCheckedCount < mAppList.size())
								mobileCheckedCount++;
						}
						Log.d(TAG, "Mobile onCheckedChanged() mobileCheckedCount=" + mobileCheckedCount);
						updateAllCheckbox(Utils.TYPE_MOBILE_ID);
						mUtils.saveData(pkgName, uid, Utils.TYPE_MOBILE, isChecked);
					} catch (RemoteException e) {
						Log.e(TAG, "MobileCheckedChangeListener-->  connected to NetworkManagementService failed");
					}
				}
			});
			return convertView;
		}
	}

	private final class ViewHolder {
		public ImageView icon;
		public TextView label;
		public CheckBox wifi;
		public CheckBox mobile;
	}

	/*
	 * 全选wifi监听器
	 */
	private class WifiAllOnClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			boolean isChecked = ((CheckBox) view).isChecked();
			Log.d(TAG, "WifiAll onCheckedChanged() isChecked=" + isChecked);
			if (isChecked) {
				for (AppInfo info : mAppList) {
					if (info.getWifiState() != true) {
						Log.d(TAG, "packageName=" + info.getPackageName() + "label" + info.getAppLabel());
						info.setWifiState(true);
						int uid = info.getUid();
						String pkgName = info.getPackageName();
						try {
							mNetworkService.setFirewallUidChainRule(uid, Utils.TYPE_WIFI_ID, true);
							mUtils.saveData(pkgName, uid, Utils.TYPE_WIFI, true);
						} catch (RemoteException e) {
							Log.e(TAG, "WifiAll-->  connected to NetworkManagementService failed");
						}
					}
				}
				wifiCheckedCount = mAppList.size();
			} else {
				Log.d(TAG, "WifiAll onCheckedChanged() wifiCheckedCount=" + wifiCheckedCount);
				for (AppInfo info : mAppList) {
					if (info.getWifiState() != false) {
						Log.d(TAG, "packageName=" + info.getPackageName() + "label" + info.getAppLabel());
						info.setWifiState(false);
						int uid = info.getUid();
						String pkgName = info.getPackageName();
						try {
							mNetworkService.setFirewallUidChainRule(uid, Utils.TYPE_WIFI_ID, false);
							mUtils.saveData(pkgName, uid, Utils.TYPE_WIFI, false);
						} catch (RemoteException e) {
							Log.e(TAG, "WifiAll-->  connected to NetworkManagementService failed");
						}
					}
				}
				wifiCheckedCount = 0;
			}
			mAdapter.notifyDataSetChanged();
		}
	}

	/*
	 * 全选移动数据监听器
	 */
	private class MobileAllOnClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			boolean isChecked = ((CheckBox) view).isChecked();
			Log.d(TAG, "MobileAll onCheckedChanged() isChecked=" + isChecked);
			if (isChecked) {
				for (AppInfo info : mAppList) {
					if (info.getMobileState() != true) {
						Log.d(TAG, "packageName=" + info.getPackageName() + "label" + info.getAppLabel());
						info.setMobileState(true);
						int uid = info.getUid();
						String pkgName = info.getPackageName();
						try {
							mNetworkService.setFirewallUidChainRule(uid, Utils.TYPE_MOBILE_ID, true);
							mUtils.saveData(pkgName, uid, Utils.TYPE_MOBILE, true);
						} catch (RemoteException e) {
							Log.e(TAG, "WifiAllOnClickListener-->  connected to NetworkManagementService failed");
						}
					}
				}
				mobileCheckedCount = mAppList.size();
			} else {
				Log.d(TAG, "MobileAll onCheckedChanged() mobileCheckedCount=" + mobileCheckedCount);
				for (AppInfo info : mAppList) {
					if (info.getMobileState() != false) {
						Log.d(TAG, "packageName=" + info.getPackageName() + "label" + info.getAppLabel());
						info.setMobileState(false);
						int uid = info.getUid();
						String pkgName = info.getPackageName();
						try {
							mNetworkService.setFirewallUidChainRule(uid, Utils.TYPE_MOBILE_ID, false);
							mUtils.saveData(pkgName, uid, Utils.TYPE_MOBILE, false);
						} catch (RemoteException e) {
							Log.e(TAG, "MobileAll-->  connected to NetworkManagementService failed");
						}
					}
				}
				mobileCheckedCount = 0;
			}
			mAdapter.notifyDataSetChanged();
		}
	}

	private void updateAllCheckbox(int type) {
		if (type == Utils.TYPE_WIFI_ID) {
			if (wifiCheckedCount == 0) {
				mWifiAll.setChecked(false);
			} else {
				mWifiAll.setChecked(true);
			}
		} else if (type == Utils.TYPE_MOBILE_ID) {
			if (mobileCheckedCount == 0) {
				mMobileAll.setChecked(false);
			} else {
				mMobileAll.setChecked(true);
			}
		}
	}

	private void getWifiAndMobileCount() {
		for (AppInfo info : mAppList) {
			if (info.getWifiState() && wifiCheckedCount < mAppList.size()) {
				wifiCheckedCount++;
			}

			if (info.getMobileState() && mobileCheckedCount < mAppList.size()) {
				mobileCheckedCount++;
			}
		}
		Log.d(TAG, "getWifiAndMobileCount()  wifiCheckedCount=" + wifiCheckedCount + "; mobileCheckedCount="
				+ mobileCheckedCount);
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
		}
		return super.onOptionsItemSelected(item);
	}
}
