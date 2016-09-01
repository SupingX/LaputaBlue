package com.laputa.blue;

import java.util.HashSet;
import java.util.Set;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.laputa.blue.core.AbstractSimpleLaputaBlue;
import com.laputa.blue.core.Configration;
import com.laputa.blue.core.OnBlueChangedListener;
import com.laputa.blue.core.SimpleLaputaBlue;
import com.laputa.blue.util.BondedDeviceUtil;
import com.laputa.blue.util.XLog;

public class BlueService extends Service {

	private AbstractSimpleLaputaBlue simpleLaputaBlue;
	private Handler mHandler = new Handler() {
	};


	@Override
	public void onCreate() {
		super.onCreate();
		acquireWakeLock();
		simpleLaputaBlue = new SimpleLaputaBlue(this, new Configration(),
				new OnBlueChangedListener() {

			@Override
			public void reconnect(HashSet<String> devices) {
				final String addressA = BondedDeviceUtil.get(1,
						getApplicationContext());
				if (BluetoothAdapter.checkBluetoothAddress(addressA)) {
					try {
						// 当前app存贮的蓝牙
						BluetoothDevice remoteDevice = simpleLaputaBlue
								.getAdapter().getRemoteDevice(addressA);
						// 所有的绑定蓝牙列表
						Set<BluetoothDevice> bondedDevices = simpleLaputaBlue
								.getAdapter().getBondedDevices();
						//
						if (bondedDevices.contains(remoteDevice)) {
							XLog.e("_____已绑定 ：" + addressA);
							if (!ifAllConnected()) {
								connect(remoteDevice);
								return;
							}
						} else {
							XLog.e("_____未绑定 ：" + addressA);
							// 当搜索列表中包含保存的addressA,并且未连接，就连接。
							if (devices.contains(addressA)) {
								if (!ifAllConnected()) {
									connect(remoteDevice);
								}
							} else {
								XLog.i("搜索列表无：" + addressA);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						XLog.e("重新连接失败！");
					}

				} else {
					XLog.i("蓝牙地址不匹配，没有addessA" + addressA);
				}
			}

			@Override
			public void onStateChanged(String address, int state) {
				
			}

			@Override
			public void onServiceDiscovered(String address) {

			}

			@Override
			public void onCharacteristicChanged(String address,
					byte[] value) {
				parseData (value);

			}

			@Override
			public boolean isAllConnected() {
				return ifAllConnected();
			}
		});
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return new BlueBinder();
	}
	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	
	public class BlueBinder extends Binder {
		public BlueService getXBlueService(){
			return BlueService.this;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		releaseWakeLock();
		closeAll();
		
	}
	
	
	
	public void startScan() {
		simpleLaputaBlue.scanDevice(true);
	}

	public void write(byte [] data){
		
		simpleLaputaBlue.write(BondedDeviceUtil.get(1, this), data);
	}
	
	public void write(byte [][] data){
		new WriteAsyncTask().execute(data);
	}

	public void stopScan() {
		simpleLaputaBlue.scanDevice(false);
	}

	public void connect(BluetoothDevice device) {
		simpleLaputaBlue.connect(device.getAddress());
	}

	public boolean ifAllConnected() {
		return simpleLaputaBlue.isConnected(BondedDeviceUtil.get(1, this));
	}

	public void closeAll() {
		simpleLaputaBlue.closeAll();
	}

	private class WriteAsyncTask extends AsyncTask<byte[][], Void, Void> {

		@Override
		protected Void doInBackground(byte[][]... params) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			byte[][] bs = params[0];
			if (bs != null && bs.length > 0) {
				for (int j = 0; j < bs.length; j++) {
					write(bs[j]);
				}
			}
			return null;
		}
	}
	
	/**
	 * <p>
	 * 解析数据
	 * </p>
	 * 
	 * @param data
	 * @param gatt
	 */
	private void parseData( byte[] data) {
		/*ProtoclNotify notify = ProtoclNotify.instance();
		XBlueBroadcastUtils xBlueBroadcastUtils = XBlueBroadcastUtils.instance();
		int dataType = notify.getDataTypeByData(data);
		if (dataType == ProtoclNotify.TYPE_MASSAGE) {
			MassageInfo massageinfo = notify.notifyMassageInfo(data);
			Log.e("", ">>>massageinfo : " + massageinfo);
			if (massageinfo != null) {
				xBlueBroadcastUtils.sendBroadcastForMassageInfo(this,massageinfo);
			}
		} else if (dataType == ProtoclNotify.TYPE_MODE_CHANGED) {
			int mode = notify.notifyModelChanged(data);
			if (mode!=-1) {
				xBlueBroadcastUtils.sendBroadcastForMode(this,mode);
			}
		} else if (dataType == ProtoclNotify.TYPE_POWER_CHANGED) {
			int power = notify.notifyPowerChanged(data);
			if (power!= -1) {
				xBlueBroadcastUtils.sendBroadcastForPower(this,power);
			}
		}else if (dataType == ProtoclNotify.TYPE_TIME_CHANGED) {
			int[] times = notify.notifyTimeChanged(data);
			if (times != null) {
				xBlueBroadcastUtils.sendBroadcastForTime(this,times);
			}
		}else if (dataType == ProtoclNotify.TYPE_HEART_RATE_CHANGED) {
			int hr = notify.notifyHeartRateChanged(data);
			if (hr != -1) {
				xBlueBroadcastUtils.sendBroadcastForHeartRate(this,hr);
			}
		}else if(dataType == ProtoclNotify.TYPE_BATTERY_CHANGED){
			int [] batteryData = notify.notifyBatteryChanged(data);
			if (batteryData!=null) {
				xBlueBroadcastUtils.sendBroadcastForBattery(this,batteryData);
			}
		}*/
	
	}
	
	  WakeLock wakeLock = null;  
	    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行  
	    private void acquireWakeLock()  
	    {  
	        if (null == wakeLock)  
	        {  
	            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);  
	            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");  
	            if (null != wakeLock)  
	            {  
	                wakeLock.acquire();  
	            }  
	        }  
	    }  
	      
	    //释放设备电源锁  
	    private void releaseWakeLock()  
	    {  
	        if (null != wakeLock)  
	        {  
	            wakeLock.release();  
	            wakeLock = null;  
	        }  
	    } 

}
