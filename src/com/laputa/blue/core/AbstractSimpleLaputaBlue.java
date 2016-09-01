package com.laputa.blue.core;

import java.lang.reflect.Method;
import java.util.Set;

import com.laputa.blue.broadcast.LaputaBroadcast;
import com.laputa.blue.util.DataUtil;
import com.laputa.blue.util.XLog;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

public abstract class AbstractSimpleLaputaBlue implements ILaputaBlue {
	private static final String TAG = AbstractSimpleLaputaBlue.class.getSimpleName();
	public static final int STATE_SERVICE_DISCOVERED = 0x0A;
	protected Configration configration;
	protected BluetoothAdapter mAdapter;
	protected BluetoothManager mManager;
	protected Context mContext;
	
	public BluetoothAdapter getAdapter(){
		return this.mAdapter;
	}
	
	/**
	 * 搜索回调
	 */
	protected LeScanCallback scanCallBack = new LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			onScanResult(device,rssi,scanRecord);
		}
	};
	/**
	 * 连接回调
	 */
	protected BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			super.onConnectionStateChange(gatt, status, newState);
			XLog.i(TAG, "==onConnectionStateChange==" +gatt.getDevice().getAddress()+ " newState :" + newState);
			doConnectionStateChange(gatt, status, newState);
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);
			XLog.i(TAG, "==onServicesDiscovered==" +gatt.getDevice().getAddress() );
			doServicesDiscovered(gatt, status);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicRead(gatt, characteristic, status);
			String hex = DataUtil.byteToHexString(characteristic.getValue());
			XLog.i(TAG, "==onCharacteristicRead==" +gatt.getDevice().getAddress()+ " value : " +hex );
			doCharacteristicRead(gatt, characteristic, status);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);
			String hex = DataUtil.byteToHexString(characteristic.getValue());
			XLog.i(TAG, "==onCharacteristicWrite==" +gatt.getDevice().getAddress()+ " value : " +hex );
			doCharacteristicWrite(gatt, characteristic, status);
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);
			String hex = DataUtil.byteToHexString(characteristic.getValue());
			XLog.i(TAG, "==onCharacteristicChanged==" +gatt.getDevice().getAddress()+ " value : " +hex );
			doCharacteristicChanged(gatt, characteristic);
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			super.onDescriptorRead(gatt, descriptor, status);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			super.onDescriptorWrite(gatt, descriptor, status);
		}

		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			super.onReliableWriteCompleted(gatt, status);
			XLog.i(TAG, "==onReliableWriteCompleted==" +"\n" +gatt.getDevice().getAddress()+ " status : " +status );
			doReliableWriteCompleted(gatt, status);
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			super.onReadRemoteRssi(gatt, rssi, status);
			XLog.i(TAG, "==onReadRemoteRssi==" +"\n" +gatt.getDevice().getAddress()+" rssi : " +rssi );
			doReadRemoteRssi(gatt, rssi, status);
			LaputaBroadcast.sendBroadcastForRemoteRssi(gatt.getDevice().getAddress(), rssi, mContext);
		}
		
	};
	
	
	public abstract void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord);
	
	protected void doReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
		
	}
	protected void doReliableWriteCompleted(BluetoothGatt gatt, int status) {
		
	}
	protected abstract void doCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) ;
	
	protected void doCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		
	}
	protected void doCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		
	}
	public abstract void stopAutoConnectTask();
	public abstract void startAutoConnectTask();
	protected abstract void doServicesDiscovered(BluetoothGatt gatt, int status);
	protected abstract void doConnectionStateChange(BluetoothGatt gatt, int status, int newState);
	
	public AbstractSimpleLaputaBlue(Context context) {
		super();
		this.mContext = context;
		checkFeature();
		initAdapter();
	}

	/**
	 * 备注：是否在执行startScan时，发现也在启动。 you should not perform discovery while
	 * connected你不应该执行发现，同时连接
	 */
	public void startDiscovery() {
		if (mAdapter != null) {
			mAdapter.startDiscovery();
		}
	}
	
	public void unregisterReceiver(){
	}
	
	public void cancelDiscovery() {
		if (mAdapter != null) {
			mAdapter.cancelDiscovery();
		}
	}
	
	public void connect(String address){
		BluetoothDevice remoteDevice = mAdapter.getRemoteDevice(address);
		this.connect(remoteDevice);
	}
	
	
	
	public Set<BluetoothDevice> getBindedDevices() {
		boolean isLog = true;
		if (mAdapter != null) {
			Set<BluetoothDevice> bondedDevices = mAdapter.getBondedDevices();
			if (isLog) {
				XLog.i("已配对设备：");
				if (bondedDevices != null && bondedDevices.size() > 0) {
					for (BluetoothDevice bluetoothDevice : bondedDevices) {
						XLog.i(bluetoothDevice.getAddress());
					}
				} else {
					XLog.e("无配对设备");
				}
			}
			return mAdapter.getBondedDevices();
		}
		return null;
	}
	
	/**
	 * 设置可见时间
	 * 可以接受BluetoothAdapter.ACTION_SCAN_MODE_CHANGED广播监听状态的改变
	 * 并有三种状态
	 * SCAN_MODE_CONNECTABLE_DISCOVERABLE, SCAN_MODE_CONNECTABLE, or SCAN_MODE_NONE
	 * 如果蓝牙尚未启用的设备，并使设备可发现将自动启用蓝牙
	 * @param duration 0:永远被发现。0~3600：可被发现0~3600秒。大于3600小于0：可被发现120秒。
	 */
	public void enableDiscoverability(int duration) {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
		mContext.startActivity(discoverableIntent);
	}
	
	/**
	 * 开启蓝牙
	 * 可以接受BluetoothAdapter.ACTION_STATE_CHANGED广播监听状态的改变
	 * @param ac
	 */
	public void enableBluetoothAdapter(Activity ac) {
		if (ac == null) {
			return;
		}
		if (isEnable()) {
			return ;
		}
		Intent intent = new Intent();
		intent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		mContext.startActivity(intent);
	}
	
	@Override
	public boolean checkFeature() {
		if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//			Toast.makeText(mContext, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
//			System.exit(0);
			onCheckFeatureFail();
			return false;
		}
		return true;
	}
	
	/**
	 * 蓝牙不可用 不支持
	 */
	protected abstract void onCheckFeatureFail();

	@Override
	public boolean isEnable() {
		if (mAdapter == null) {
			return false;
		}
		return mAdapter.isEnabled();
	}
	
	public static boolean isEnable(Context context){
		int osVersion = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
		if (osVersion <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
			return mAdapter.isEnabled();
		} else {
			BluetoothManager mManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
			BluetoothAdapter mAdapter = mManager.getAdapter();
			return mAdapter.isEnabled();
		}
	}
	
	public static boolean checkFeature(Context context) {
		if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//			Toast.makeText(mContext, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
//			System.exit(0);
			return false;
		}
		return true;
	}
	
	
	@Override
	public void enableBluetooth() {
		if (!isEnable()) {
			mAdapter.enable();
		}
	}

	@Override
	public void disableBluetooth() {
		if (isEnable()) {
			mAdapter.disable();
		}
	}

	/*@Override
	public boolean isAllConnected() {
		return false;
	}*/
	
	
/*	@Override
	public synchronized void connect(BluetoothDevice device) {
		
		if (device == null) {
			XLog.e("========connect===============device 为空null"  );
			return ;
		}
		String address = device.getAddress();
		XLog.e("========开始连接=========="+address+"=====");
		
		BluetoothGatt connectGatt = device.connectGatt(mContext, false, gattCallBack);
		if (connectGatt != null) {
			onBeginConnect(device,connectGatt);
		}
		
	}*/

	public BluetoothDevice getRemoteDevice(String address){
		if(mAdapter != null && BluetoothAdapter.checkBluetoothAddress(address)){
			return mAdapter.getRemoteDevice(address);
		}
		return null;
	}
	
	/**
	 * 初始化adapter
	 */
	public void initAdapter() {
		mAdapter = null;
		mManager = null;
		int osVersion = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
		if (osVersion <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			mAdapter = BluetoothAdapter.getDefaultAdapter();
		} else {
			mManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
			mAdapter = mManager.getAdapter();
		}
	}
	
	public abstract void stopScanTask();
	
	
	/**
	 * 清除蓝牙蓝牙缓存
	 * 参见:http://blog.csdn.net/zzfenglin/article/details/51105893
	 */
	public boolean refreshDeviceCache(BluetoothGatt mBluetoothGatt) {  
	    if (mBluetoothGatt != null) {  
	        try {  
	            BluetoothGatt localBluetoothGatt = mBluetoothGatt;  
	            Method localMethod = localBluetoothGatt.getClass().getMethod(  
	                    "refresh", new Class[0]);  
	            if (localMethod != null) {  
	                boolean bool = ((Boolean) localMethod.invoke(  
	                        localBluetoothGatt, new Object[0])).booleanValue();  
	                return bool;  
	            }  
	        } catch (Exception localException) {  
	            Log.i(TAG, "An exception occured while refreshing device");  
	        }  
	    }  
	    return false;  
	} 
	
	//获取数据
	public void readCharacteristic(BluetoothGattCharacteristic characteristic,BluetoothGatt gatt) {
		gatt.readCharacteristic(characteristic);
	}
	
	
}
