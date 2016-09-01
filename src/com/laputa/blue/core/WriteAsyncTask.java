package com.laputa.blue.core;

import com.laputa.blue.util.XLog;

import android.os.AsyncTask;

public class WriteAsyncTask extends AsyncTask<byte[][], Integer, Boolean> {
	
	private long interval = DEFALUT_INTERVAL;
	private AbstractSimpleLaputaBlue simpleLaputaBlue;
	private String address;
	public static final long DEFALUT_INTERVAL = 500;
	private int index = 0;
	
	public WriteAsyncTask(AbstractSimpleLaputaBlue simpleLaputaBlue,String address){
		new WriteAsyncTask(simpleLaputaBlue, address, DEFALUT_INTERVAL);
	}
	
	public WriteAsyncTask(AbstractSimpleLaputaBlue simpleLaputaBlue,String address,long interval){
		this.simpleLaputaBlue = simpleLaputaBlue;
		this.address = address;
		this.interval = interval;
	}
	
	public WriteAsyncTask(AbstractSimpleLaputaBlue simpleLaputaBlue,String address,long interval,OnWriteListener mOnWriteListener){
		this.simpleLaputaBlue = simpleLaputaBlue;
		this.address = address;
		this.interval = interval;
		this.mOnWriteListener = mOnWriteListener;
	}
	
	@Override
	protected Boolean doInBackground(byte[][]... params) {
		byte[][] bs = params[0];
		if (bs != null && bs.length >0) {
			size = bs.length;
			for (int i = 0; i < bs.length; i++) {
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					return false;
				}
				simpleLaputaBlue.write(address,bs[i]);
				index++;
				publishProgress(index);
				if (index==size) {
					index=0;
				}
			}
		}
		
		return true;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (mOnWriteListener != null) {
			mOnWriteListener.onResult(result);
			XLog.i("写数据结果 : " +result);
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (mOnWriteListener != null) {
			mOnWriteListener.onUpdate(size,values[0]);
			XLog.i("写数据进度 : " +index+",总的进度["+size+"]");
		}
		super.onProgressUpdate(values);
	}
	
	public interface OnWriteListener {
		void onResult(boolean result);
		void onUpdate(int total , int index);
	}
	
	public OnWriteListener mOnWriteListener;
	private int size;
	public void setOnWriteListener(OnWriteListener l){
		this.mOnWriteListener = l;
	}
}
