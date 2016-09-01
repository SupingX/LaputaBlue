package com.laputa.blue.core;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

public interface ILaputaBlue {
	/**
	 * 检查是否支持ＢＬＥ蓝牙
	 */
	boolean checkFeature();
	
	boolean isEnable();
	
	void enableBluetooth();
	
	void disableBluetooth();
	
	void scanDevice(boolean status);
	
	boolean isAllConnected();
	
	boolean isConnected(String address);
	
	void connect(BluetoothDevice device);
	
	void write(String address,byte [] value);
	
	void write(String address,byte[] [] values);
	
	void close(String address);
	
	void closeAll();
	
	void readRemoteRssi(String address);
}
