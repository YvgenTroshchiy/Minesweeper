package com.quver.miner.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.quver.miner.R;
import com.quver.miner.game.Cell;
import com.quver.miner.game.MineFieldAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class NetworkGameActivity extends Activity {
	
	// Message types sent from the BluetoothChatService Handler
	public static final int		MESSAGE_STATE_CHANGE	= 1;
	public static final int		MESSAGE_READ			= 2;
	public static final int		MESSAGE_WRITE			= 3;
	public static final int		MESSAGE_DEVICE_NAME		= 4;
	public static final int		MESSAGE_TOAST			= 5;
	
	// Key names received from the BluetoothChatService Handler
	public static final String	DEVICE_NAME				= "device_name";
	public static final String	TOAST					= "toast";
	
	private String					mConnectedDeviceName	= null;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_DEVICE_NAME:
					// save the connected device's name
					mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
					Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
					
					startActivity(new Intent(NetworkGameActivity.this, GameSettings.class));
					break;
	            case MESSAGE_READ:
	            	//TODO
	                byte[] readBuf = (byte[]) msg.obj;
	                // construct a string from the valid bytes in the buffer
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                //TODO
//	                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
	                break;
			}
		};
	};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_bluetooth);
	}
	
}
