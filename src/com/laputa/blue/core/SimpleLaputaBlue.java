package com.laputa.blue.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.laputa.bean.ConnectInfo;
import com.laputa.blue.broadcast.LaputaBroadcast;
import com.laputa.blue.util.BondedDeviceUtil;
import com.laputa.blue.util.XLog;

public class SimpleLaputaBlue extends AbstractSimpleLaputaBlue {
	private final String TAG = SimpleLaputaBlue.class.getSimpleName();
	/** scan_deff内的蓝牙集合  **/
	private HashSet<String> deviceSet = new HashSet<String>();
	/** 已连接蓝牙集合  **/
	private HashMap<String, ConnectInfo> connectResults = new HashMap<String, ConnectInfo>();
	private OnBlueChangedListener mOnBlueChangedListener;
	private static final int SCAN_DEFF = 12 * 1000;
	public boolean AUTO_CONNECT = true;
	
	public void clearConnectMap() {
		connectResults.clear();
	}

	public void stopAutoConnectTask(){
		AUTO_CONNECT = false;
		if (scanTask != null) {
			mHandler.removeCallbacks(scanTask);
		}
	}
	
	public void startAutoConnectTask(){
		AUTO_CONNECT = true;
		if (scanTask != null) {
			mHandler.removeCallbacks(scanTask);
			mHandler.post(scanTask);
		}
	}
	
	private Handler mHandler = new Handler() {

	};

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(LaputaBroadcast.ACTION_LAPUTA_PREFERENCE_ADDRESS)) {
				int count = intent.getExtras().getInt(
						LaputaBroadcast.EXTRA_LAPUTA_PREFERENCE_ADDRESS);
				if (count > 0) {
					mHandler.removeCallbacks(scanTask);
					mHandler.post(scanTask);
				} else {
					mHandler.removeCallbacks(scanTask);
				}
			}
		}

	};
	
	public void unregisterReceiver() {
		super.unregisterReceiver();
		mContext.unregisterReceiver(receiver);
	}

	/**
	 * <p>
	 * 搜索任务，每隔SCAN_DEFF更新一次scanDevices
	 * </p>
	 * 用于重新连接蓝牙，搜索结束时，判断蓝牙是否连接。
	 */
	private Runnable scanTask;;
	private boolean scanning;
	public SimpleLaputaBlue(Context context) {
		this(context, new Configration());
	}

	public SimpleLaputaBlue(Context context, Configration configration) {
		this(context, configration, null);
	}

	public SimpleLaputaBlue(Context context, Configration configration,
			OnBlueChangedListener l) {
		super(context);
		mContext.registerReceiver(receiver, LaputaBroadcast.getIntentFilter());
		this.configration = configration;
		this.mOnBlueChangedListener = l;
		// if (isEnable()) {
		scanTask = new Runnable() {
			@Override
			public void run() {
				if (AUTO_CONNECT) {
					if (BondedDeviceUtil.getPreferenceCount(mContext) != 0) {
						if (!isAllConnected()) {
							XLog.i(SimpleLaputaBlue.class, "未全部连接成功，继续扫描...");
							// 清空设备列表
							// deviceSet.clear();
							scanDevice(true);
						}
					}
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (scanning) {
								scanDevice(false);
							}
							mHandler.removeCallbacks(scanTask);
							// 发送广播，当前时间段发现的设备
							// LaputaBroadcast.sendBroadcastForDevicesFound(scanDevices,
							// mContext);
							// 断线重新连接
							if (BondedDeviceUtil.getPreferenceCount(mContext) != 0) {
								if (!isAllConnected()) {
									if (deviceSet.size() > 0) {
										reconnect(deviceSet);
										deviceSet.clear();

									}
								}
							}

							//						mHandler.postDelayed(scanTask, 4000);
							mHandler.post(scanTask);
						}

					}, SCAN_DEFF);
				}
			}
		};

		mHandler.post(scanTask);
		
		// checkScan();
		// }
		
	
	}

	/**
	 * 检查是否开启蓝牙搜索。 比如现在存贮地址有1个，就开启。 如果是0个，就无需开启。
	 */
	/*private void checkScan() {
		int preferenceCount = BondedDeviceUtil.getPreferenceCount(mContext);
		if (preferenceCount > 0) {
			mHandler.post(scanTask);
		} else {
			XLog.i("没有存贮任何蓝牙");
		}
	}*/

	public SimpleLaputaBlue(Context context, OnBlueChangedListener l) {
		this(context, new Configration(), l);

	}

	@Override
	public  void connect(BluetoothDevice device) {

		if (device == null) {
			XLog.e("========> connect()_device 为空null");
			return;
		}
		String address = device.getAddress();

		XLog.i("========> 开始连接..." + address);
		ConnectInfo connectInfo = connectResults.get(address);

		if (connectInfo == null) {
			XLog.i("========>连接信息不存在");
			BluetoothGatt connectGatt = device.connectGatt(mContext, false,
					gattCallBack);
			connectInfo = new ConnectInfo(connectGatt, address,
					BluetoothProfile.STATE_DISCONNECTED);
			connectResults.put(address, connectInfo);
		} else {
			XLog.i("========>连接信息已存在");
			close(address);
			connectResults.remove(address);
			BluetoothGatt connectGatt = device.connectGatt(mContext, false,
					gattCallBack);
			connectInfo = new ConnectInfo(connectGatt, address,
					BluetoothProfile.STATE_DISCONNECTED);
			connectResults.put(address, connectInfo);
			
			/*close(address);
			BluetoothGatt connectGatt = device.connectGatt(mContext, false,
					gattCallBack);
			connectInfo = new ConnectInfo(connectGatt, address,
					BluetoothProfile.STATE_DISCONNECTED);
			connectResults.put(address, connectInfo);*/
			// removeDevice(address);
		/*	BluetoothGatt gatt = connectInfo.getGatt();
			if (gatt==null) {
				return ;
			}
			if (address.equals(gatt.getDevice().getAddress())) {
				// if (reconnectCount >= 1) {
				// close(address);
				// } else {
				gatt.connect();
				// }
			}*/
		
		}
		printConnectInfo();
	}
	boolean showInfo = false;
	private void printConnectInfo() {

		if (showInfo) {
			Set<Entry<String, ConnectInfo>> entrySet = connectResults
					.entrySet();
			Iterator<Entry<String, ConnectInfo>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Entry<String, ConnectInfo> next = iterator.next();
				String key = next.getKey();
				ConnectInfo value = next.getValue();
				XLog.i(SimpleLaputaBlue.class, "========>所有的连接信息 +\n"
						+ "address :" + key + ",ConnectInfo :" + value + "/n");
			}
		}
	}

	/*
	 * @Override protected void onBeginConnect(BluetoothDevice device,
	 * BluetoothGatt gatt) {
	 * 
	 * String address = device.getAddress(); // 添加绑定地址 //
	 * BondedDeviceUtil.addAddressOne(mContext, address);
	 * 
	 * // 将这个连接信息result添加进连接设备的列表 removeDevice(address);
	 * 
	 * ConnectInfo result = new ConnectInfo(gatt, address,
	 * BluetoothProfile.STATE_DISCONNECTED); XLog.e("onBeginConnect 的Gatt ：" +
	 * result.getGatt()); connectResults.put(address, result);
	 * 
	 * }
	 */

	@Override
	protected void doCharacteristicChanged(BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic) {
		if (mOnBlueChangedListener != null) {
			String address = gatt.getDevice().getAddress();
			byte[] value = characteristic.getValue();
			mOnBlueChangedListener.onCharacteristicChanged(address, value);
		}
	}

	@Override
	protected void doServicesDiscovered(BluetoothGatt gatt, int status) {
		String address = gatt.getDevice().getAddress();
		if (status == BluetoothGatt.GATT_SUCCESS) {
			BluetoothGattService service = gatt.getService(UUID
					.fromString(configration.UUID_SERVICE));
			if (service != null) {
				// 设置service
				BluetoothGattCharacteristic characteristicWrite = service
						.getCharacteristic(UUID
								.fromString(configration.UUID_CHARACTERISTIC_WRITE));
				BluetoothGattCharacteristic characteristicNotify = service
						.getCharacteristic(UUID
								.fromString(configration.UUID_CHARACTERISTIC_NOTIFY));
				if (characteristicWrite != null && characteristicNotify != null) {
					characteristicWrite
							.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

					gatt.setCharacteristicNotification(characteristicNotify,
							true);
					BluetoothGattDescriptor descriptor = characteristicNotify
							.getDescriptor(UUID
									.fromString(configration.UUID_DESC_CCC));
					if (descriptor != null) {
						descriptor
								.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						gatt.writeDescriptor(descriptor);

						// 更新connectResults的值。
						updateConnectDevice(address, gatt,
								STATE_SERVICE_DISCOVERED);
						// 发送通知
						LaputaBroadcast.sendBroadcastForStateChanged(address,
								STATE_SERVICE_DISCOVERED, mContext);
						// 回调，由service来实现 。
						if (mOnBlueChangedListener != null) {
							mOnBlueChangedListener.onServiceDiscovered(address);
						}
					} else {
						XLog.e("无法匹配descriptor！");
						// updateConnectDevice(address, gatt,
						// BluetoothProfile.STATE_DISCONNECTED);
						close(address);
						LaputaBroadcast.sendBroadcastForStateChanged(address,
								BluetoothProfile.STATE_DISCONNECTED, mContext);
					}
				} else {
					XLog.e("无法匹配characteristicNotify characteristicWrite！");
					// updateConnectDevice(address, gatt,
					// BluetoothProfile.STATE_DISCONNECTED);
					close(address);
					LaputaBroadcast.sendBroadcastForStateChanged(address,
							BluetoothProfile.STATE_DISCONNECTED, mContext);
				}

			} else {
				XLog.e("无法匹配service！");
				// updateConnectDevice(address, gatt,
				// BluetoothProfile.STATE_DISCONNECTED);
				close(address);
				LaputaBroadcast.sendBroadcastForStateChanged(address,
						BluetoothProfile.STATE_DISCONNECTED, mContext);
			}
		} else {
			XLog.e("查找服务失败！discoverServices() Fail !");
			// updateConnectDevice(address, gatt,
			// BluetoothProfile.STATE_DISCONNECTED);
			close(address);
			LaputaBroadcast.sendBroadcastForStateChanged(address,
					BluetoothProfile.STATE_DISCONNECTED, mContext);
		}
	}

	/**
	 * 更新连接状态
	 * 
	 * @param gatt
	 */
	private void updateConnectDevice(String address, BluetoothGatt gatt,
			int state) {
		if (connectResults != null && !connectResults.isEmpty()) {
			ConnectInfo connectInfo = connectResults.get(address);
			if (connectInfo != null) {
				connectInfo.setState(state);
			}
		}
		printConnectInfo();
	}

	@Override
	protected void doConnectionStateChange(final BluetoothGatt gatt, int status,
			int newState) {

		XLog.i("连接信息：doConnectionStateChange" + "--status:" + status
				+ ",newState:" + newState);
		final String address = gatt.getDevice().getAddress();
		if (status == BluetoothGatt.GATT_SUCCESS) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						
						gatt.discoverServices();
					}
				}, 500);
				
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				close(address);
//				refreshDeviceCache(gatt);
			}

			XLog.i(" [连接状态] : [" + newState
					+ "]————————> 0:断开 ；2：连接");
//			updateConnectDevice(address, gatt, newState);
			LaputaBroadcast.sendBroadcastForStateChanged(address, newState,
					mContext);
		} else {
			XLog.e("获取连接state失败");
			// updateConnectDevice(address, gatt,
			// BluetoothProfile.STATE_DISCONNECTED);
			close(address);
//			refreshDeviceCache(gatt);
			LaputaBroadcast.sendBroadcastForStateChanged(address,
					BluetoothProfile.STATE_DISCONNECTED, mContext);
		}
	}

	@Override
	public void scanDevice(boolean status) {
		XLog.e(TAG, "scanDevice()");
		if (isEnable()) {
			mAdapter.stopLeScan(scanCallBack);
			scanning = false;
			if (status) {
				mAdapter.startLeScan(scanCallBack);
//				scanDeviceWithUUIDs();
				scanning = status;
			}
			LaputaBroadcast.sendBroadcastForIsScanning(scanning, mContext);
		} else {
			XLog.e(TAG, "scanDevice() -- 蓝牙不可用");
		}
	}
	
	/**
	 * 根据UUID搜索蓝牙
	 */
	public void scanDeviceWithUUIDs(){
			mAdapter.startLeScan(new UUID[]{UUID.fromString(configration.UUID_SERVICE)}, scanCallBack);
	}

	@Override
	public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
		LaputaBroadcast.sendBroadcastForDeviceFound(device, mContext, rssi);
		// if (!scanDevices.contains(device)) {
		// scanDevices.add(device);
		// }
		if (!deviceSet.contains(device.getAddress())) {
			deviceSet.add(device.getAddress());
			
		}
	}

	@Override
	public void write(String address, byte[] value) {
		printConnectInfo();
		if (connectResults.containsKey(address)) {
			ConnectInfo connectInfo = connectResults.get(address);
			if (connectInfo != null) {
				if (connectInfo.getState() == STATE_SERVICE_DISCOVERED) {
					BluetoothGatt gatt = connectInfo.getGatt();
					if (gatt ==null ) {
						return ;
					}
					// XLog.i("SimpleLaputaBlue", "-->写数据 gatt :" + gatt);
					BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(gatt);
					XLog.i("SimpleLaputaBlue", "-->写数据 characteristic :"
							+ characteristic);
					if (characteristic != null) {
						// characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
						characteristic.setValue(value);
						gatt.writeCharacteristic(characteristic);
					} else {
						XLog.d("SimpleLaputaBlue", address + "-->写数据异常");
					}
				} else {
					XLog.d("SimpleLaputaBlue", "-->未连接 ：" + address);
				}
			} else {
				XLog.d("SimpleLaputaBlue", "-->不存在这个地址的connectInfo ："
						+ connectInfo);
			}
		} else {
			XLog.d("SimpleLaputaBlue", "-->不存在这个地址的address ：" + address);
		}
	}

	@Override
	public void write(String address, byte[][] values) {

		new WriteAsyncTask(this, address, 500, new WriteAsyncTask.OnWriteListener() {
			
			@Override
			public void onUpdate(int size,int index) {
		
			}
			
			@Override
			public void onResult(boolean result) {
		
			}
		}).execute(values);
	}

	@Override
	public void close(String address) {
		if (connectResults.containsKey(address)) {
			ConnectInfo connectInfo = connectResults.get(address);
			BluetoothGatt gatt = connectInfo.getGatt();
			if (gatt != null) {
			BluetoothDevice device = gatt.getDevice();
		
				XLog.i("close() --" + address);
				gatt.close();
				refreshDeviceCache(gatt);
				
				//解除绑定
				try {
					BondedDeviceUtil.removeBond(device);
				} catch (Exception e) {
					XLog.e(SimpleLaputaBlue.class, "关闭gatt异常，地址：" + address);
					e.printStackTrace();
				}
				LaputaBroadcast.sendBroadcastForStateChanged(address,
						BluetoothProfile.STATE_DISCONNECTED, mContext);
			}
			connectResults.remove(address);
		
		}
	}

	@Override
	public void closeAll() {
		if (connectResults != null && connectResults.size() > 0) {
			Set<Entry<String, ConnectInfo>> entrySet = connectResults.entrySet();
			Iterator<Entry<String, ConnectInfo>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Entry<String, ConnectInfo> next = iterator.next();
				ConnectInfo value = next.getValue();
				if (value!=null) {
					BluetoothGatt gatt = value.getGatt();
					if (gatt != null) {
						gatt.close();
						//解除绑定
						try {
							BluetoothDevice device = gatt.getDevice();
							BondedDeviceUtil.removeBond(device);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		connectResults.clear();
	}

	public void setOnBlueChangedListener(
			OnBlueChangedListener mOnBlueChangedListener) {
		this.mOnBlueChangedListener = mOnBlueChangedListener;
	}

	/*
	 * private void removeDevice(String address) { if
	 * (connectResults.containsKey(address)) { ConnectInfo result =
	 * connectResults.get(address);
	 * result.setState(BluetoothProfile.STATE_DISCONNECTED); if (result != null)
	 * { BluetoothGatt gatt = result.getGatt(); gatt.close(); gatt = null; }
	 * connectResults.remove(address);
	 * LaputaBroadcast.sendBroadcastForStateChanged(address,
	 * BluetoothProfile.STATE_DISCONNECTED, mContext); } }
	 */

	private BluetoothGattCharacteristic getBluetoothGattCharacteristic(
			BluetoothGatt gatt) {
		if (gatt == null) {
			XLog.i("SimpleLaputaBlue", "-->gatt为空");
			return null;
		}
		BluetoothGattService service = gatt.getService(UUID
				.fromString(configration.UUID_SERVICE));
		if (service == null) {
			XLog.e("SimpleLaputaBlue", "-->service为空");
			return null;
		}
		BluetoothGattCharacteristic characteristic = service
				.getCharacteristic(UUID
						.fromString(configration.UUID_CHARACTERISTIC_WRITE));
		if (characteristic == null) {
			XLog.e("SimpleLaputaBlue", "-->characteristic为空");
			return null;
		}
		return characteristic;
	}

	private void reconnect(HashSet<String> devices) {

		if (mOnBlueChangedListener != null) {
			mOnBlueChangedListener.reconnect(devices);
			return;
		}

		/*
		 * XLog.i(devices.size()+"个"); if (devices.size()>0) { Iterator<String>
		 * iterator = devices.iterator(); while (iterator.hasNext()) {
		 * XLog.i(iterator.next());
		 * 
		 * } } XLog.i("---------------------------------------");
		 * 
		 * // if (scanDevices != null && scanDevices.size() > 0) { String
		 * addressA = BondedDeviceUtil.get(1, mContext); String addressB =
		 * BondedDeviceUtil.get(2, mContext); XLog.i(SimpleLaputaBlue.class,
		 * "reconnect() --> addressA :" + addressA + ",是否连接:" +
		 * isConnected(addressA)); XLog.i(SimpleLaputaBlue.class,
		 * "reconnect() --> addressB :" + addressB + ",是否连接:" +
		 * isConnected(addressB));
		 * 
		 * if (BluetoothAdapter.checkBluetoothAddress(addressA)) { if
		 * (devices.contains(addressA)) { if (!isConnected(addressA)) {
		 * connect(addressA); } }else{ XLog.i("搜索列表无："+addressA); }
		 * 
		 * }else{ XLog.i("未绑定"+addressA); }
		 * 
		 * if (BluetoothAdapter.checkBluetoothAddress(addressB)) { if
		 * (devices.contains(addressB)) { if (!isConnected(addressB) ) {
		 * connect(addressB); } }else{ XLog.i("搜索列表无："+addressB); } }else{
		 * XLog.i("未绑定"+addressB); }
		 * 
		 * 
		 * for (BluetoothDevice device : scanDevices) { if
		 * (!isConnected(addressA) // && device.getAddress().equals(addressA) )
		 * { connect(addressA); break; }
		 * 
		 * // if (!isConnected(addressB) // &&
		 * device.getAddress().equals(addressB)) { // connect(device); // } }
		 * for (BluetoothDevice device : scanDevices) { // if
		 * (!isConnected(addressA) // && device.getAddress().equals(addressA)) {
		 * // connect(device); // }
		 * 
		 * if (!isConnected(addressB) && device.getAddress().equals(addressB)) {
		 * connect(addressB); break; } } // }
		 */
	}

	@Override
	public boolean isAllConnected() {
		if (mOnBlueChangedListener != null) {
			boolean allConnected = mOnBlueChangedListener.isAllConnected();
			XLog.e("allConnected	:" + allConnected);
			return mOnBlueChangedListener.isAllConnected();
		}

		/*
		 * String addressA = BondedDeviceUtil.get(1, mContext); String addressB
		 * = BondedDeviceUtil.get(2, mContext); // if (addressA.equals("") &&
		 * addressB.equals("")) { // return false; // } if
		 * ((!TextUtils.isEmpty(addressA)&&isConnected(addressA)) &&
		 * (!TextUtils.isEmpty(addressB)&&isConnected(addressB))) { return true;
		 * }
		 */
		return false;
	}

	@Override
	public boolean isConnected(String address) {
		if (connectResults.containsKey(address)) {
			ConnectInfo connectInfo = connectResults.get(address);
			return connectInfo.getState() == STATE_SERVICE_DISCOVERED;
		}
		return false;
	}

	@Override
	public void readRemoteRssi(String address) {
		if (address == null || address.equals("")) {
			XLog.e("========readRemoteRssi===============address 为空null");
			return;
		}

		// XLog.i("========开始readRemoteRssi==========" + address + "=====");
		ConnectInfo connectInfo = connectResults.get(address);

		if (connectInfo == null) {

		} else {
			BluetoothGatt gatt = connectInfo.getGatt();
			if (gatt != null
					&& connectInfo.getState() == STATE_SERVICE_DISCOVERED) {
				gatt.readRemoteRssi();
			}
		}
	}

	@Override
	public void stopScanTask() {
		XLog.e("***************stopScanTask()***************");
//		mHandler.removeCallbacks(scanTask);
		mHandler.removeCallbacksAndMessages(null);
		scanTask = null;

	}

	@Override
	protected void onCheckFeatureFail() {
		
	}
	
	
	
}
