package com.laputa.blue;

import java.util.ArrayList;
import java.util.Set;

import com.laputa.blue.broadcast.LaputaBroadcast;
import com.laputa.blue.core.Configration;
import com.laputa.blue.core.AbstractSimpleLaputaBlue;
import com.laputa.blue.core.SimpleLaputaBlue;
import com.laputa.blue.util.BondedDeviceUtil;
import com.laputa.blue.util.DataUtil;
import com.laputa.blue.util.XLog;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, android.content.Intent intent) {
			String action = intent.getAction();
			if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				XLog.e("ACTION_DISCOVERY_FINISHED  -->  搜索结束");
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				XLog.e("ACTION_DISCOVERY_STARTED  -->  搜索开始");
			} else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
				XLog.e("ACTION_CONNECTION_STATE_CHANGED  -->  连接状态改变");
			} else if (action.equals(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)) {
				XLog.e("ACTION_LOCAL_NAME_CHANGED  -->  名字改变");
			} else if (action.equals(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)) {
				XLog.e("ACTION_REQUEST_DISCOVERABLE  -->  打开蓝牙可见性");
			} else if (action.equals(BluetoothAdapter.ACTION_REQUEST_ENABLE)) {
				XLog.e("ACTION_REQUEST_ENABLE  -->  ");
			} else if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
				int scanMode = intent.getExtras().getInt(BluetoothAdapter.EXTRA_SCAN_MODE);
				int preScanMode = intent.getExtras().getInt(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE);
				XLog.e("ACTION_SCAN_MODE_CHANGED  -->  模式改变" + "\n scanMode :" + scanMode + "preScanMode : " + preScanMode);
			} else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
				int preState = intent.getExtras().getInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE);
				String stateInfo = "之前的状态：" + preState;
				if (state == BluetoothAdapter.STATE_OFF) {
					stateInfo += "\n 关闭 :" + state;
				} else if (state == BluetoothAdapter.STATE_ON) {
					stateInfo += "\n 打开 :" + state;
					blue.startDiscovery();
				} else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
					stateInfo += "\n 关闭中 :" + state;
				} else if (state == BluetoothAdapter.STATE_TURNING_ON) {
					stateInfo += "\n 打开中 :" + state;
				}
				XLog.e("ACTION_STATE_CHANGED  -->  adapter状态 " + stateInfo);

			} else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				XLog.i("ACTION_FOUND  -->  找到设备 ");
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String name = intent.getExtras().getString(BluetoothDevice.EXTRA_NAME);
				short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
				XLog.i("device : " + device.getName() + "," + device.getAddress());
				XLog.i("name : " + name);
				XLog.i("rssi : " + rssi);

			} else if (action.equals(LaputaBroadcast.ACTION_LAPUTA_DEVICES_FOUND)) {
				ArrayList<BluetoothDevice> datas = intent.getParcelableArrayListExtra(LaputaBroadcast.EXTRA_LAPUTA_DEVICES);
				if (datas != null) {
					XLog.e("datas : " + datas.size());
				}

				if (datas != null) {
					devices.clear();
					devices.addAll(datas);

//					adapter.notifyDataSetChanged();
				}
			} else if (action.equals(LaputaBroadcast.ACTION_LAPUTA_IS_SCANING)) {
				final boolean scanning = intent.getExtras().getBoolean(LaputaBroadcast.EXTRA_LAPUTA_SCANING);
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						if (scanning) {
							lvDevice.setBackgroundColor(Color.GREEN);
						} else {
							lvDevice.setBackgroundColor(Color.BLACK);
						}

					}
				});
			} else if (action.equals(LaputaBroadcast.ACTION_LAPUTA_STATE)) {
				final int state = intent.getExtras().getInt(LaputaBroadcast.EXTRA_LAPUTA_STATE);
				final String address = intent.getExtras().getString(LaputaBroadcast.EXTRA_LAPUTA_ADDRESS);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						StringBuffer info = new StringBuffer();
						info.append("address : " + address);
						String stateStr = "未知";
						if (state == BluetoothProfile.STATE_CONNECTED) {
							stateStr = "已连接";
						} else if (state == BluetoothProfile.STATE_DISCONNECTED) {
							stateStr = "未连接";
						} else if (state == AbstractSimpleLaputaBlue.STATE_SERVICE_DISCOVERED) {
							stateStr = "已找到服务";
						}
						info.append("\n state :" + stateStr);
						tvInfo.setText(info.toString());
					}
				});
			}
		};
	};
	private AbstractSimpleLaputaBlue blue;
	private Handler mHandler = new Handler() {
	};
//	private MyAdapter adapter;
	private ListView lvDevice;
	private TextView tvInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
		register();
		Configration conf = new Configration();
		// blue = new AbstractSimple(MainActivity.this,conf);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {

//				blue = ((BaseApp) getApplication()).getSimpleBlueService().getSimpleLaputaBlue();
			}
		}, 2000);

//		lvDevice = (ListView) findViewById(R.id.lv_device);
//		tvInfo = (TextView) findViewById(R.id.tv_info);
//		adapter = new MyAdapter();
//		lvDevice.setAdapter(adapter);
//		lvDevice.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				BluetoothDevice device = devices.get(position);
//				blue.connect(device);
//				// BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED
//			}
//		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	public void test2(View v) {
		
	}

	public void test3(View v) {
		byte[] value1 = DataUtil.hexStringToByte("45");
		byte[] value2 = DataUtil.hexStringToByte("45");
		byte[] value3 = DataUtil.hexStringToByte("45");
		
	}

	public void test(View v) {
		// Set<BluetoothDevice> bindedDevices = blue.getBindedDevices();
		// blue.enableDiscoverability(6);

		// blue.enableBluetoothAdapter(this);
		// BluetoothGatt gatt = null;
		byte[] value1 = DataUtil.hexStringToByte("45");

	}

	private void register() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		filter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(LaputaBroadcast.ACTION_LAPUTA_DEVICES_FOUND);
		filter.addAction(LaputaBroadcast.ACTION_LAPUTA_IS_SCANING);
		filter.addAction(LaputaBroadcast.ACTION_LAPUTA_STATE);
		registerReceiver(receiver, filter);
	}

	private void gattServer() {
		/*
		 * oolean executeReliableWrite = gatt.executeReliableWrite();
		 * 
		 * BluetoothAdapter defaultAdapter =
		 * BluetoothAdapter.getDefaultAdapter();
		 * 
		 * 
		 * BluetoothManager manager = (BluetoothManager)
		 * getSystemService(Context.BLUETOOTH_SERVICE);
		 * 
		 * BluetoothGattServer gattServer = manager.openGattServer(this, new
		 * BluetoothGattServerCallback() {
		 * 
		 * @Override public void onConnectionStateChange(BluetoothDevice device,
		 * int status, int newState) { super.onConnectionStateChange(device,
		 * status, newState); }
		 * 
		 * @Override public void onServiceAdded(int status, BluetoothGattService
		 * service) { super.onServiceAdded(status, service); }
		 * 
		 * @Override public void onCharacteristicReadRequest(BluetoothDevice
		 * device, int requestId, int offset, BluetoothGattCharacteristic
		 * characteristic) { super.onCharacteristicReadRequest(device,
		 * requestId, offset, characteristic); }
		 * 
		 * @Override public void onCharacteristicWriteRequest(BluetoothDevice
		 * device, int requestId, BluetoothGattCharacteristic characteristic,
		 * boolean preparedWrite, boolean responseNeeded, int offset, byte[]
		 * value) { super.onCharacteristicWriteRequest(device, requestId,
		 * characteristic, preparedWrite, responseNeeded, offset, value); }
		 * 
		 * @Override public void onDescriptorReadRequest(BluetoothDevice device,
		 * int requestId, int offset, BluetoothGattDescriptor descriptor) {
		 * super.onDescriptorReadRequest(device, requestId, offset, descriptor);
		 * }
		 * 
		 * @Override public void onDescriptorWriteRequest(BluetoothDevice
		 * device, int requestId, BluetoothGattDescriptor descriptor, boolean
		 * preparedWrite, boolean responseNeeded, int offset, byte[] value) {
		 * super.onDescriptorWriteRequest(device, requestId, descriptor,
		 * preparedWrite, responseNeeded, offset, value); }
		 * 
		 * @Override public void onExecuteWrite(BluetoothDevice device, int
		 * requestId, boolean execute) { super.onExecuteWrite(device, requestId,
		 * execute); }
		 * 
		 * });
		 */
	}

	/*private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return devices.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return devices.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_device, parent, false);
				holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tvAddress = (TextView) convertView.findViewById(R.id.tv_address);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			BluetoothDevice device = devices.get(position);
			String name = device.getName();
			String address = device.getAddress();
			holder.tvName.setText((name == null || name.equals("")) ? "Laputa" : name);
			holder.tvAddress.setText(address);
			return convertView;
		}

		class ViewHolder {
			TextView tvName;
			TextView tvAddress;
		}

	}*/
}
