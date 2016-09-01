package com.laputa.blue.broadcast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.laputa.blue.util.XLog;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class LaputaBroadcast {
	public static final String ACTION_LAPUTA_DEVICES_FOUND = "ACTION_LAPUTA_DEVICES_FOUND";
	public static final String ACTION_LAPUTA_DEVICE_FOUND = "ACTION_LAPUTA_DEVICE_FOUND";
	public static final String ACTION_LAPUTA_IS_SCANING = "ACTION_LAPUTA_IS_SCANING";
	public static final String ACTION_LAPUTA_STATE = "ACTION_LAPUTA_STATE";
	public static final String ACTION_LAPUTA_REMOTE_RSSI = "ACTION_LAPUTA_REMOTE_RSSI";
	public static final String ACTION_LAPUTA_PREFERENCE_ADDRESS = "ACTION_LAPUTA_PREFERENCE_ADDRESS";
	
	
	
	public static final String EXTRA_LAPUTA_DEVICES = "EXTRA_LAPUTA_DEVICES";
	public static final String EXTRA_LAPUTA_DEVICE = "EXTRA_LAPUTA_DEVICE";
	public static final String EXTRA_LAPUTA_SCANING = "EXTRA_LAPUTA_SCANING";
	public static final String EXTRA_LAPUTA_ADDRESS = "EXTRA_LAPUTA_ADDRESS";
	public static final String EXTRA_LAPUTA_STATE = "EXTRA_LAPUTA_STATE";
	public static final String EXTRA_LAPUTA_RSSI = "EXTRA_LAPUTA_RSSI";
	public static final String EXTRA_LAPUTA_PREFERENCE_ADDRESS = "EXTRA_LAPUTA_PREFERENCE_ADDRESS";
	
	private static LaputaBroadcast lb;
/*	private LaputaBroadcast(){
		
	}
	public static LaputaBroadcast newInstance(String [] actions){
		if (lb == null) {
			lb = new LaputaBroadcast();
			lb.addAction(actions);
		}
		return lb;
	}*/
	
	
	
	public static void sendBroadcastForRemoteRssi(String address, int rssi,Context context) {
		Intent intent = new Intent(ACTION_LAPUTA_REMOTE_RSSI);
		intent.putExtra(EXTRA_LAPUTA_RSSI, rssi);
		intent.putExtra(EXTRA_LAPUTA_ADDRESS, address);
		context.sendBroadcast(intent);
	}
	
	public static void sendBroadcastForDevicesFound(ArrayList<BluetoothDevice> devices,Context context){
		Intent intent = new Intent();
		intent.setAction(ACTION_LAPUTA_DEVICES_FOUND);
		intent.putParcelableArrayListExtra(EXTRA_LAPUTA_DEVICES, devices);
		context.sendBroadcast(intent);
	}
	
	public  static void sendBroadcastForDeviceFound(BluetoothDevice device,Context context,int rssi){
//		XLog.i(LaputaBroadcast.class,"sendBroadcastForDeviceFound():" + device.getAddress() + " , rssi:"+rssi   );
		Intent intent = new Intent();
		intent.setAction(ACTION_LAPUTA_DEVICE_FOUND);
		intent.putExtra(EXTRA_LAPUTA_DEVICE, device);
		intent.putExtra(EXTRA_LAPUTA_RSSI, rssi);
		context.sendBroadcast(intent);
	}
	
	public static void sendBroadcastForIsScanning(boolean scanning,Context context){
		Intent intent = new Intent();
		intent.setAction(ACTION_LAPUTA_IS_SCANING);
		intent.putExtra(EXTRA_LAPUTA_SCANING, scanning);
		context.sendBroadcast(intent);
	}
	
	public static void sendBroadcastForStateChanged(String address,int state,Context context){
		XLog.e("sendBroadcastForStateChanged()     -->  state : " +state); 
		Intent intent = new Intent();
		intent.setAction(ACTION_LAPUTA_STATE);
		intent.putExtra(EXTRA_LAPUTA_ADDRESS, address);
		intent.putExtra(EXTRA_LAPUTA_STATE, state);
		context.sendBroadcast(intent);
	}
	
	public static IntentFilter getIntentFilter(){
		Iterator<String> iterator = actions.iterator();
		IntentFilter filter = new IntentFilter();
		while (iterator.hasNext()) {
			String action = iterator.next();
			filter.addAction(action);
		}
		return filter;
	}
	
	private  static Set<String> actions = new HashSet<String>();
	
	static{
		actions.add(ACTION_LAPUTA_DEVICES_FOUND);
		actions.add(ACTION_LAPUTA_DEVICE_FOUND);
		actions.add(ACTION_LAPUTA_IS_SCANING);
		actions.add(ACTION_LAPUTA_STATE);
		actions.add(ACTION_LAPUTA_REMOTE_RSSI);
		actions.add(ACTION_LAPUTA_PREFERENCE_ADDRESS);
	}
	
	public  static void addAction(String action){
		actions.add(action);
	}
	public  static void addAction(String action []){
		for (int i = 0; i < action.length; i++) {
			actions.add(action[i]);
		}
	}
	
	/**
	 * 保存/解除地址的时候，发送的通知
	 * @param context
	 * @param count
	 */
	public static void sendBroadcastWhenPreferenceAddress(Context context,int count) {
		
	}
	
	

}
