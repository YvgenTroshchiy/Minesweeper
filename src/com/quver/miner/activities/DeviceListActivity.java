package com.quver.miner.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.util.Set;

public class DeviceListActivity extends Activity implements OnClickListener {

	public final static String		EXTRA_DEVICE_ADDRESS	= "device_address";
	private static final int		REQUEST_ENABLE_BT		= 3;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
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
	private Button vBtnDiscoverble;
	private Button vBtnScanButton;
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			mBtAdapter.cancelDiscovery();
			
			// Get the device MAC address, which is the last 17 chars in the  View
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);

			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};
	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			
		};
	};

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
	
}
