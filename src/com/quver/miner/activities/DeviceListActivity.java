package com.quver.miner.activities;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quver.miner.R;

public class DeviceListActivity extends Activity implements OnClickListener {
	private static final String TAG = "DeviceListActivity";
	
	private final UUID				MY_UUID	= UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
	private static final String 	NAME = "Bluetooth";
	public final static String		EXTRA_DEVICE_ADDRESS	= "device_address";
	public static final String		DEVICE_NAME				= "device_name";
	private static final int		REQUEST_ENABLE_BT		= 3;
	public static final int			MESSAGE_DEVICE_NAME		= 4;
	
	private Button					vBtnDiscoverble;
	private Button					vBtnScanButton;
	
	private BluetoothDevice			mDevice;
	private String					mConnectedDeviceName	= null;
	private BluetoothAdapter		mBtAdapter;
	private ArrayAdapter<String>	mPairedDevicesArrayAdapter;
	private ArrayAdapter<String>	mNewDevicesArrayAdapter;
	private AcceptThread 			mAcceptThread;
	private ConnectThread 			mConnectThread;
	
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_DEVICE_NAME:
					// save the connected device's name
					mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
					Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
					
					startActivity(new Intent(DeviceListActivity.this, GameSettings.class));
					break;
			}
		};
	};
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			mBtAdapter.cancelDiscovery();
			
			// Get the device MAC address, which is the last 17 chars in the  View
			String info = ((TextView) v).getText().toString();
			String macAddress = info.substring(info.length() - 17);
			
			mDevice = mBtAdapter.getRemoteDevice(macAddress);
			mConnectThread = new ConnectThread(mDevice);
			mConnectThread.start();
			//TODO make spinner
			Toast.makeText(DeviceListActivity.this, "Start Connecting", Toast.LENGTH_SHORT).show();
		}
	};
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed already
				if (mDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
					mNewDevicesArrayAdapter.add(mDevice.getName() + "\n" + mDevice.getAddress());
				}
			} else
				if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					// TODO When discovery is finished, close dialog or spinner
					if (mNewDevicesArrayAdapter.getCount() == 0) {
						mNewDevicesArrayAdapter.add(getResources().getString(R.string.none_found));
					}
				}
		}
	};
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list);
		
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		if (!mBtAdapter.isEnabled()) {
			startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
		}
		
		vBtnDiscoverble = (Button) findViewById(R.id.btn_discoverable);
		vBtnDiscoverble.setOnClickListener(this);
		vBtnScanButton = (Button) findViewById(R.id.btn_button_scan);
		vBtnScanButton.setOnClickListener(this);
		
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		
		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);
		
		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);
		
		// Register for broadcasts
		registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		} else {
			mPairedDevicesArrayAdapter.add(getResources().getString(R.string.none_paired));
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}
		unregisterReceiver(mReceiver);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_discoverable:
				Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
				startActivity(discoverableIntent);
				break;
			case R.id.btn_button_scan:
				//TODO Show dialog or spinner
				if (mBtAdapter.isDiscovering()) {
					mBtAdapter.cancelDiscovery();
				}
				mBtAdapter.startDiscovery();
				break;
		}
	}
	
	public void connected(BluetoothSocket socket, BluetoothDevice device) {
		Log.d("myLogs", "connected to Socket");
		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		
		Message msg = mHandler.obtainMessage(DeviceListActivity.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(DeviceListActivity.DEVICE_NAME, mDevice.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
		
		Log.d("myLogs", "connect");
		
		startActivity(new Intent(this, GameSettings.class));
		
//		(new ConnectedThread(mSocket, mHandler)).start();
	}
	
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mServerSocket;
		
		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			// Create a new listening server socket
			try {
				// MY_UUID is the app's UUID string, also used by the client code
				tmp = mBtAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			mServerSocket = tmp;
		}
		
		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					socket = mServerSocket.accept();
					Log.e(TAG, "mServerSocket.accept()");
				}
				catch (IOException e) {
					Log.e(TAG, "Socket accept() failed");
				}
				// If a connection was accepted
				if (socket != null) {
					cancel();
					break;
				}
			}
		}
		
		public void cancel() {
			try {
				mServerSocket.close();
			}
			catch (IOException e) {}
		}
	}
	
	private class ConnectThread extends Thread {
		//	private final BluetoothAdapter	mBluetoothAdapter;		
		private final BluetoothSocket	mSocket;
		private final BluetoothDevice	mDevice;
		
		public ConnectThread(BluetoothDevice device) {
			BluetoothSocket tmp = null;
			mDevice = device;
			
			//	Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
			}
			catch (Exception e) {
				Log.e(TAG, "Socket create() failed");
			}
			mSocket = tmp;
		}
		
		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");
			mBtAdapter.cancelDiscovery();
			try {
				mSocket.connect();
			}
			catch (IOException e) {
				cancel();
				//TODO connectionFailed();
				Log.d(TAG, "Connection Failed");
				return;
			}
			connected(mSocket, mDevice);
		}
		
		public void cancel() {
			try {
				mSocket.close();
			}
			catch (IOException e) {
				Log.e(TAG, "close() of connect " + mSocket + " socket failed", e);
			}
		}
	}
}
