package com.laputa.blue.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;

public class CommonUtil {
	/**
	 * 判断用户是否在前台
	 * 
	 * @param context
	 * @return
	 */
	public static  boolean isRunningForeground(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(context.getPackageName())) {
			return true;
		}
		return false;
	}
}
