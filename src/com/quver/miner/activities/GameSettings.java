package com.quver.miner.activities;

import com.quver.miner.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class GameSettings extends Activity implements OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_settings);
		
		TextView opponent = (TextView) findViewById(R.id.text_opponent);
		
		Spinner spinnerPlayerType = (Spinner) findViewById(R.id.spinner_playerType);
		Spinner spinnerGridSize = (Spinner) findViewById(R.id.spinner_gridSize);
		
		Button btnStartGame = (Button) findViewById(R.id.btn_startGame);
		btnStartGame.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		
	}
	
}
