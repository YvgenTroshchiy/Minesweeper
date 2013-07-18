package com.quver.miner.activities;

import java.util.ArrayList;
import java.util.Iterator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.quver.miner.game.Cell;
import com.quver.miner.game.MineFieldAdapter;
import com.quver.miner.R;
import com.quver.miner.activities.GameSettings;

public class SetMines extends Activity implements OnItemClickListener {
	
	private GridView			vMineField;
	private int					mGridSize;
	private ArrayList<Cell>		mCellsArray;
	private ArrayList<Integer>	mMinesPosition	= new ArrayList<Integer>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_field);
		
		Intent intent = getIntent();
		mGridSize = intent.getIntExtra(GameSettings.GRID_SIZE, 0);
		
		Button vBtnField = (Button) findViewById(R.id.btn_field);
		vBtnField.setText(getString(R.string.start));
		vBtnField.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMinesPosition.size() >= mGridSize * mGridSize / 2) {
					Toast.makeText(SetMines.this, getResources().getString(R.string.Number_of_mines_shood_be), Toast.LENGTH_LONG).show();
					return;
				}
				if (mMinesPosition.size() == 0) {
					Toast.makeText(SetMines.this, getResources().getString(R.string.you_dont_set_mines), Toast.LENGTH_LONG).show();
					return;
				}
				Intent intent = new Intent();
				intent.putIntegerArrayListExtra(GameSettings.MINES_ARRAY, mMinesPosition);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		
		int mMinDim = Math.min(width, height);
		mMinDim -= getResources().getDimension(R.dimen.activity_horizontal_margin);
		int mCellSize = mMinDim / mGridSize;
		
		vMineField = (GridView) findViewById(R.id.mineField);
		vMineField.setNumColumns(mGridSize);
		
		int mCellsCount = mGridSize * mGridSize;
		mCellsArray = new ArrayList<Cell>(mCellsCount);
		for (int i = 0; i < mCellsCount; i++) {
			mCellsArray.add(new Cell());
		}
		
		MineFieldAdapter mMineFieldAdapter = new MineFieldAdapter(this, mCellsArray, mCellSize);
		vMineField.setAdapter(mMineFieldAdapter);
		vMineField.setOnItemClickListener(this);
	}
	
	private void remooveValueFromminesPosition(Integer positionToRemoove) {
		for (Iterator<Integer> i = mMinesPosition.iterator(); i.hasNext();) {
			Integer integer = (Integer) i.next();
			if (integer == positionToRemoove) {
				i.remove();
			}
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Cell cell = mCellsArray.get(position);
		
		if (cell.isMine()) {
			v.setBackgroundResource(R.drawable.cell_selector);
			cell.setMine(false);
			remooveValueFromminesPosition(position);
		} else {
			v.setBackgroundResource(R.drawable.cell_mine);
			cell.setMine(true);
			mMinesPosition.add(position);
		}
	}
	
}
