package com.laputa.blue.util;

import android.util.Log;

public class XLog {
	public static boolean DEV_MODE = true;
	public static boolean DEV_MODE_E = true;
	public static boolean DEV_MODE_V = true;
	public static boolean DEV_MODE_I = true;
	public static boolean DEV_MODE_D = true;
	public static final String TAG = "laputa";

	public static void e(String tag, String msg) {
		if (DEV_MODE && DEV_MODE_E) {
			Log.e(tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if (DEV_MODE && DEV_MODE_V) {
			Log.e(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (DEV_MODE && DEV_MODE_I) {
			Log.e(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (DEV_MODE && DEV_MODE_D) {
			Log.d(tag, msg);
		}
	}

	public static void e(String msg) {
		if (DEV_MODE && DEV_MODE_E) {
			Log.e(TAG, msg);
		}
	}

	public static void v(String msg) {
		if (DEV_MODE && DEV_MODE_V) {
			Log.v(TAG, msg);
		}
	}

	public static void i(String msg) {
		if (DEV_MODE && DEV_MODE_I) {
			Log.i(TAG, msg);
		}
	}

	public static void d(String msg) {
		if (DEV_MODE && DEV_MODE_D) {
			Log.d(TAG, msg);
		}
	}

	public static void e(Class<?> clz, String msg) {
		if (DEV_MODE && DEV_MODE_E) {
			Log.e(clz.getSimpleName(), msg);
		}
	}

	public static void v(Class<?> clz, String msg) {
		if (DEV_MODE && DEV_MODE_V) {
			Log.v(clz.getSimpleName(), msg);
		}
	}

	public static void i(Class<?> clz, String msg) {
		if (DEV_MODE && DEV_MODE_I) {
			Log.i(clz.getSimpleName(), msg);
		}
	}

	public static void d(Class<?> clz, String msg) {
		if (DEV_MODE && DEV_MODE_D) {
			Log.d(clz.getSimpleName(), msg);
		}
	}
}
