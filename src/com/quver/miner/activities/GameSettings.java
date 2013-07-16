package com.quver.miner.activities;

import com.quver.miner.R;
import com.quver.miner.bluetooth.BluetoothService;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GameSettings extends Activity implements OnClickListener {
	private static final String	TAG						= "GameSettings";
	
	// Message types sent from the BluetoothChatService Handler
	public static final int		MESSAGE_STATE_CHANGE	= 1;
	public static final int		MESSAGE_READ			= 2;
	public static final int		MESSAGE_WRITE			= 3;
	public static final int		MESSAGE_DEVICE_NAME		= 4;
	public static final int		MESSAGE_TOAST			= 5;
	
	// Key names received from the BluetoothChatService Handler
	public static final String	DEVICE_NAME				= "device_name";
	public static final String	TOAST					= "toast";
	
	// Intent request codes
	private static final int	REQUEST_CONNECT_DEVICE	= 2;
	private static final int	REQUEST_ENABLE_BT		= 3;
	
	private BluetoothAdapter	mBluetoothAdapter		= null;
	private BluetoothService	mService				= null;
	private String				mConnectedDeviceName	= null;
	private StringBuffer		mOutStringBuffer;
	
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
//            case MESSAGE_WRITE:
//                byte[] writeBuf = (byte[]) msg.obj;
//                // construct a string from the buffer
//                String writeMessage = new String(writeBuf);
//                mConversationArrayAdapter.add("Me:  " + writeMessage);
//                break;
//            case MESSAGE_READ:
//                byte[] readBuf = (byte[]) msg.obj;
//                // construct a string from the valid bytes in the buffer
//                String readMessage = new String(readBuf, 0, msg.arg1);
//                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
//                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
	
	@Override
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_settings);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}
	
	public void initializeView() {
		TextView opponent = (TextView) findViewById(R.id.text_opponent);
		
		Button btnChooseOponent = (Button) findViewById(R.id.btn_chooseOponent);
		btnChooseOponent.setOnClickListener(this);
		
		Spinner spinnerPlayerType = (Spinner) findViewById(R.id.spinner_playerType);
		Spinner spinnerGridSize = (Spinner) findViewById(R.id.spinner_gridSize);
		
		Button btnStartGame = (Button) findViewById(R.id.btn_startGame);
		btnStartGame.setOnClickListener(this);
		
		mService = new BluetoothService(this, mHandler);
        mOutStringBuffer = new StringBuffer("");
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Log.e(TAG, "++ ON START ++");
		
		// If BT is not on, request that it be enabled.
		// initializeView() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mService == null) initializeView();
		}
	}
	
    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mService != null) {
            if (mService.getState() == BluetoothService.STATE_NONE) {
              mService.start();
            }
        }
    }
	
    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.e(TAG, "- ON PAUSE -");
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mService != null) {
			mService.stop();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
			case REQUEST_CONNECT_DEVICE:
				if (resultCode == Activity.RESULT_OK) {
					connectDevice(data, false);
				}
				break;
			case REQUEST_ENABLE_BT:
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a chat session
					initializeView();
				} else {
					// User did not enable Bluetooth or an error occurred
					Log.d(TAG, "BT not enabled");
					Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
					finish();
				}
		}
	}
	
	private void connectDevice(Intent data, boolean secure) {
		String MACaddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(MACaddress);
		mService.connect(device);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_chooseOponent:
				// Launch the DeviceListActivity to see devices and do scan
				startActivityForResult(new Intent(this, DeviceListActivity.class), REQUEST_CONNECT_DEVICE);
				break;
		}
		
	}
	
}
