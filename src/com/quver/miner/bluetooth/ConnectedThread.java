package com.quver.miner.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;

public class ConnectedThread extends Thread {
	public static final int			MESSAGE_READ	= 2;
	
	private final BluetoothSocket	mSocket;
	private final InputStream		mInStream;
	private final OutputStream		mOutStream;
//	private final Handler			mHandler;
	
	public ConnectedThread(BluetoothSocket socket) {
		mSocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
//		mHandler = handler;
		
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		mInStream = tmpIn;
		mOutStream = tmpOut;
	}
	
	public void run() {
		byte[] buffer = new byte[1024]; //buffer store for the stream;
		int bytes; //bytes returned from read();
		
		//Keep listening to the InpuStream until an exception occurs
		while (true) {
			try {
				bytes = mInStream.read(buffer);
//				mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
			}
			catch (IOException e) {
				break;
			}
		}
	}
	
	/* Call this from the main activity to send data to the remote device */
	public void write(byte[] bytes) {
		try {
			mOutStream.write(bytes);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* Call this from the main activity to shutdown the connection */
	public void cancel() {
		try {
			mSocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
