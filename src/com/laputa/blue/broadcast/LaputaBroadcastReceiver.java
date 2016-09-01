package com.laputa.blue.broadcast;

import java.util.ArrayList;


import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LaputaBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(LaputaBroadcast.ACTION_LAPUTA_DEVICE_FOUND)) {
			BluetoothDevice device = intent.getParcelableExtra(LaputaBroadcast.EXTRA_LAPUTA_DEVICE);
			onDeviceFound(device);
		} else if (action.equals(LaputaBroadcast.ACTION_LAPUTA_DEVICES_FOUND)) {
			ArrayList<BluetoothDevice> datas = intent.getParcelableArrayListExtra(LaputaBroadcast.EXTRA_LAPUTA_DEVICES);
			onDevicesFound(datas);
		} else if (action.equals(LaputaBroadcast.ACTION_LAPUTA_IS_SCANING)) {
			final boolean scanning = intent.getExtras().getBoolean(LaputaBroadcast.EXTRA_LAPUTA_SCANING);
			onScaningChanged(scanning);
		} else if (action.equals(LaputaBroadcast.ACTION_LAPUTA_STATE)) {
			final int state = intent.getExtras().getInt(LaputaBroadcast.EXTRA_LAPUTA_STATE);
			final String address = intent.getExtras().getString(LaputaBroadcast.EXTRA_LAPUTA_ADDRESS);
			onStateChanged(address,state);
		}
	}
	
	
	protected void onDeviceFound(BluetoothDevice device) {
		
	}

	protected void onStateChanged(String address, int state) {
		
	}

	protected void onScaningChanged(boolean scanning) {
		
	}

	protected void onDevicesFound(ArrayList<BluetoothDevice> datas) {
		
	}

}
