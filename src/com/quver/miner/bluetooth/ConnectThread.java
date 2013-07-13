package com.quver.miner.bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class ConnectThread extends Thread {
	private static final String		TAG		= "ConnectThread";
	private static final UUID		MY_UUID	= UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
	
	//	private final BluetoothAdapter	mBluetoothAdapter;													;
	private final BluetoothSocket	mSocket;
	private final BluetoothDevice	mDevice;
	private final Handler			mHandler;
	
	public ConnectThread(BluetoothDevice device, Handler handler) {
		BluetoothSocket tmp = null;
		mDevice = device;
		mHandler = handler;
		
		//	Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
		}
		catch (Exception e) {
			// Unable to connect; close the socket and get out
			Log.e(TAG, "Socket create() failed");
			cancel();
		}
		mSocket = tmp;
	}
	
	public void run() {
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
		try {
			mSocket.connect();
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		new ConnectedThread(mSocket, mHandler).start();
	}
	
	public void cancel() {
		try {
			mSocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
