package com.quver.miner.activities;

import com.quver.miner.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.btn_singleGame).setOnClickListener(this);
		findViewById(R.id.btn_networkGame).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_singleGame:
				startActivity(new Intent(this, SingleGameActivity.class));
				break;
			case R.id.btn_networkGame:
				startActivity(new Intent(this, GameSettings.class));
				break;
		}
	}
	
}
