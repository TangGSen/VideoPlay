package com.video.player;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetUtil {

	/**
	 * 妫�祴缃戠粶杩炴帴鏄惁鍙敤
	 */
	public static boolean isNetworkConnected(Activity context) {
		if (null != context) {
			ConnectivityManager manager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = manager.getActiveNetworkInfo();
			if (null != info) {
				Log.e("鎻愮ず", "缃戠粶杩炴帴鐘舵�涓猴細[" + info.isAvailable() + "]");
				return info.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 妫�祴wifi鏄惁宸茬粡杩炴帴
	 */
	public static boolean isWifiConnected(Context context) {
		if (null != context) {
			ConnectivityManager manager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = manager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (null != info) {
				return info.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 鑾峰彇褰撳墠缃戠粶杩炴帴鐨勭被鍒�
	 */
	public static int getConnectedType(Context context) {
		if (null != context) {
			ConnectivityManager manager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = manager.getActiveNetworkInfo();
			if (null != info && info.isAvailable()) {
				return info.getType();
			}
		}
		return -1;
	}

}
