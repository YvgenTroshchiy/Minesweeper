package com.quver.miner.activities;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.quver.miner.R;

public class DeviceListActivity extends Activity implements OnClickListener {
	private static final String		TAG						= "DeviceListActivity";
	public final static String		EXTRA_DEVICE_ADDRESS	= "device_address";
	
	private Button					vBtnDiscoverble;
	private Button					vBtnScanButton;
	
	private BluetoothAdapter		mBluetoothAdapter;
	private ArrayAdapter<String>	mPairedDevicesArrayAdapter;
	private ArrayAdapter<String>	mNewDevicesArrayAdapter;
	
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
					setProgressBarIndeterminateVisibility(false);
					if (mNewDevicesArrayAdapter.getCount() == 0) {
						String noDevices = getResources().getText(R.string.none_found).toString();
						mNewDevicesArrayAdapter.add(noDevices);
					}
				}
		}
	};
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			mBluetoothAdapter.cancelDiscovery();
			
			String info = ((TextView) v).getText().toString();
			String MACaddress = null;
			if(info.isEmpty()){
				Log.d(TAG, "String info is empty");
				return;
			}else {
				MACaddress = info.substring(info.length() - 17);
			}
			
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, MACaddress);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);
		
		setResult(Activity.RESULT_CANCELED);
		
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
		this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		
		// Get the local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
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
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
		}
		this.unregisterReceiver(mReceiver);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_discoverable:
				if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
					Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
					discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
					startActivity(discoverableIntent);
				}
				break;
			case R.id.btn_button_scan:
				// Indicate scanning in the title
				setProgressBarIndeterminateVisibility(true);
				
				if (mBluetoothAdapter.isDiscovering()) {
					mBluetoothAdapter.cancelDiscovery();
				}
				mBluetoothAdapter.startDiscovery();
				break;
		}
	}
	
}
