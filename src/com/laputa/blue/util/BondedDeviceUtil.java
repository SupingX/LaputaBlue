package com.laputa.blue.util;

import java.lang.reflect.Method;

import com.laputa.blue.broadcast.LaputaBroadcast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;
/**
 * 保存蓝牙地址，最多可保存4个地址。
 * @author Administrator
 *
 */
public class BondedDeviceUtil {
	public static final String SHARE_ADDRESS_0 = "SHARE_ADDRESS_0";
	public static final String SHARE_ADDRESS_01 = "SHARE_ADDRESS_01";
	public static final String SHARE_ADDRESS_02 = "SHARE_ADDRESS_02";
	public static final String SHARE_ADDRESS_03 = "SHARE_ADDRESS_03";
	public static final String SHARE_ADDRESS_04 = "SHARE_ADDRESS_04";
	public static final String SHARE_ADDRESSES = "SHARE_ADDRESSES";
	
	/**
	 * 存贮地址
	 * @param position 位置[1-4]
	 * @param address  有效的蓝牙地址
	 * @param context  
	 */
	public static void save(int position,String address,Context context){
		/*boolean checkBluetoothAddress = BluetoothAdapter.checkBluetoothAddress(address);
		if (!checkBluetoothAddress) {
			XLog.i("save()--要存贮的蓝牙地址无效");
			return ;
		}*/
		String sh = "";
		if (position == 1) {
			sh = SHARE_ADDRESS_01;
		} else if (position == 2) {
			sh = SHARE_ADDRESS_02;
		}else if (position == 3) {
			sh = SHARE_ADDRESS_03;
		}else if (position == 4) {
			sh = SHARE_ADDRESS_04;
		}else {
			XLog.i("save()--要存贮的position无效:[1-4]");
			return ;
		}
		
		SharedPreferenceUtil.put(context, sh, address);
		int count = getPreferenceCount(context);
		LaputaBroadcast.sendBroadcastWhenPreferenceAddress(context,count);
	}
	
	/**
	 * 删除存贮地址
	 * @param position
	 * @param context
	 */
	public static void delete(int position,Context context){
		save(position, "", context);
	}
	
	/**
	 * 获取存贮地址
	 * @param position
	 * @param context
	 * @return
	 */
	public static String get(int position,Context context){
		String sh = "";
		if (position == 1) {
			sh = SHARE_ADDRESS_01;
		} else if (position == 2) {
			sh = SHARE_ADDRESS_02;
		} else if (position == 3) {
			sh = SHARE_ADDRESS_03;
		} else if (position == 4) {
			sh = SHARE_ADDRESS_04;
		} else {
			XLog.i("save()--要存贮的position无效:[1-4]");
			return "";
		}
		String json = (String) SharedPreferenceUtil.get(context, sh, "");
		return json;
	}
	
	/**
	 * 获取存贮地址的数量
	 * @param context
	 * @return count[0~4]
	 */
	public static int getPreferenceCount(Context context){
		int count=0;
		if (!TextUtils.isEmpty(get(1, context))) {
			count++;
		}
		if (!TextUtils.isEmpty(get(2, context))) {
			count++;
		}
		if (!TextUtils.isEmpty(get(3, context))) {
			count++;
		}
		if (!TextUtils.isEmpty(get(4, context))) {
			count++;
		}
		return count;
	}
	
	
	/**
	 * 取消绑定进程
	 * @param device
	 * @return
	 * @throws Exception
	 */
	public static boolean cancelBondProcess( BluetoothDevice device) throws Exception  {
		Method createBondMethod = BluetoothDevice.class.getMethod("cancelBondProcess");
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}
	
	/**
	 * 不要输出框
	 * @param btClass
	 * @param device
	 * @return
	 * @throws Exception
	 */
	public static boolean cancelPairingUserInput(BluetoothDevice device) throws Exception{
		Method createBondMethod = BluetoothDevice.class.getMethod("cancelPairingUserInput");
		return (Boolean) createBondMethod.invoke(device);
	}
	
	/**
	 * 解除绑定
	 * @param btClass
	 * @param btDevice
	 * @return
	 * @throws Exception
	 */
	public static boolean removeBond( BluetoothDevice btDevice) throws Exception {
		XLog.e("==========> 解除绑定 ：" + btDevice.getAddress());
		Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
		Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}
	
	
}
