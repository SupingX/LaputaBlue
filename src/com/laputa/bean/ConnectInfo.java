package com.laputa.bean;

import android.bluetooth.BluetoothGatt;

public class ConnectInfo {
	private BluetoothGatt gatt;
	private String address;
	private int state;
	public ConnectInfo(BluetoothGatt gatt, String address, int state) {
		super();
		this.gatt = gatt;
		this.address = address;
		this.state = state;
	}
	public ConnectInfo() {
		super();
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public BluetoothGatt getGatt() {
		return gatt;
	}
	public void setGatt(BluetoothGatt gatt) {
		this.gatt = gatt;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
}
