package com.quver.miner.activities;

import com.quver.miner.R;
import com.quver.miner.bluetooth.BluetoothService;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GameSettings extends Activity implements OnClickListener {
	private static final String		TAG						= "GameSettings";
	
	//	Constant for intent
	public final static String		PLAYER_TYPE				= "player_type";
	public final static String		GRID_SIZE				= "grid_size";
	
	public final static String		MINES_ARRAY				= "mines_array";
	//	Constant for player type
	public static final int			PLAYER_TYPE_MINER		= 0;
	public static final int			PLAYER_TYPE_SAPPER		= 1;
	
	//	Constant for grid size
	public static final int			GRID_SIZE_8x8			= 0;
	public static final int			GRID_SIZE_16x16			= 1;
	public static final int			GRID_SIZE_32x32			= 2;
	public static final int			GRID_SIZE_64x64			= 3;
	public static final int			GRID_SIZE_128x128		= 4;
	public static final int			GRID_SIZE_256x256		= 5;
	
	// Message types sent from the BluetoothChatService Handler
	public static final int			MESSAGE_STATE_CHANGE	= 1;
	public static final int			MESSAGE_READ			= 2;
	public static final int			MESSAGE_WRITE			= 3;
	public static final int			MESSAGE_DEVICE_NAME		= 4;
	public static final int			MESSAGE_TOAST			= 5;
	
	// Key names received from the BluetoothChatService Handler
	public static final String		DEVICE_NAME				= "device_name";
	public static final String		TOAST					= "toast";
	
	// Intent request codes
	private static final int		REQUEST_CONNECT_DEVICE	= 2;
	public static final int			REQUEST_ENABLE_BT		= 3;
	public final static int			SET_MINES_ARRAY			= 4;
	public final static int			NETWORK_GAME			= 5;
	
	private Spinner					vSpinnerPlayerType;
	private Spinner					vSpinnerGridSize;
	
	private int						mPlayerType;
	private int						mGridSize;
	
	private BluetoothAdapter		mBluetoothAdapter		= null;
	public static BluetoothService	mService				= null;
	private String					mConnectedDeviceName	= null;
	private StringBuffer			mOutStringBuffer		= new StringBuffer("");
	
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
			case GameSettings.MESSAGE_READ:
				//TODO
				byte[] readBuf = (byte[]) msg.obj;
				
				Intent intent = new Intent(GameSettings.this, NetworkGameActivity.class);
				intent.putExtra(GRID_SIZE, mGridSize);
				//TODO
//				intent.putIntegerArrayListExtra(MINES_ARRAY, value);
				startActivityForResult(intent, NETWORK_GAME);
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
		
		Button vBtnChooseOponent = (Button) findViewById(R.id.btn_chooseOponent);
		vBtnChooseOponent.setOnClickListener(this);
		
		vSpinnerPlayerType = (Spinner) findViewById(R.id.spinner_playerType);
		vSpinnerGridSize = (Spinner) findViewById(R.id.spinner_gridSize);
		
		Button vBtnStartGame = (Button) findViewById(R.id.btn_startGame);
		vBtnStartGame.setOnClickListener(this);
		
		mService = BluetoothService.getInstance(this, mHandler);
//        mOutStringBuffer = new StringBuffer("");
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
			if (mService == null) {
				initializeView();
			}
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
	
	private void sendMessage(Integer[] message) {
		//TODO
		if (mService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}
		
//			byte[] send = message.;
//			mService.write(send);
			mOutStringBuffer.setLength(0);
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
			case SET_MINES_ARRAY:
				//TODO
				Toast.makeText(this, getResources().getString(R.string.waiting_for_the_game_result), Toast.LENGTH_LONG).show();
				//				sendMessage(data.getIntArrayExtra(MINES_ARRAY));
				break;
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
			case R.id.btn_startGame:
				
				switch (vSpinnerPlayerType.getSelectedItemPosition()) {
					case PLAYER_TYPE_MINER:
						mPlayerType = PLAYER_TYPE_MINER;
						break;
					case PLAYER_TYPE_SAPPER:
						mPlayerType = PLAYER_TYPE_SAPPER;
						
						Toast toast = Toast.makeText(this, getResources().getString(R.string.wait_until_set_mines), Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
						break;
				}
				
				switch (vSpinnerGridSize.getSelectedItemPosition()) {
					case GRID_SIZE_8x8:
						mGridSize = 8;
						break;
					case GRID_SIZE_16x16:
						mGridSize = 16;
						break;
					case GRID_SIZE_32x32:
						mGridSize = 32;
						break;
					case GRID_SIZE_64x64:
						mGridSize = 64;
						break;
					case GRID_SIZE_128x128:
						mGridSize = 128;
						break;
					case GRID_SIZE_256x256:
						mGridSize = 256;
						break;
				}
				
				Intent intent = new Intent(this, SetMines.class);
				intent.putExtra(GRID_SIZE, mGridSize);
				startActivityForResult(intent, SET_MINES_ARRAY);
				break;
		}
	}
	
}
