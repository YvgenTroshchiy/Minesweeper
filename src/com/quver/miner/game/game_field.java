package com.quver.miner.game;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.quver.miner.R;

import java.util.ArrayList;
import java.util.Random;

public class game_field extends Activity implements OnItemClickListener, OnItemLongClickListener {

	private static final String	TAG			= "myLogs";
	//TODO make choose from list for count
	private GridView			vMineField;
	private int					mGridSize	= 8;
	private int					mCellsCount;
	private int					mMaxMinsCount;
	private ArrayList<Cell>		mMineField;
	private int					mMinDim;
	private int					mCellSize;
	private MineFieldAdapter	mMineFieldAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_field);

		mCellsCount = mGridSize * mGridSize;
		mMaxMinsCount = mCellsCount / 2;

		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		mMinDim = Math.min(width, height);
		mCellSize = mMinDim / mGridSize;

		vMineField = (GridView) findViewById(R.id.mineField);
		vMineField.setNumColumns(mGridSize);

		mMineField = new ArrayList<Cell>(mCellsCount);
		for (int i = 0; i < mCellsCount; i++) {
			mMineField.add(new Cell());
		}

		mMineFieldAdapter = new MineFieldAdapter(this, mMineField, mCellSize);
		vMineField.setAdapter(mMineFieldAdapter);
		vMineField.setOnItemClickListener(this);
		vMineField.setOnItemLongClickListener(this);

		setRandomMines();
	}

	private void setRandomMines() {
		Random r = new Random();
		int minesCount = mMaxMinsCount / 2 + r.nextInt(mMaxMinsCount / 2);
		for (int i = 0; i < minesCount; i++) {
			int p = r.nextInt(mCellsCount);
			mMineField.get(p).setMine(true);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		v.setEnabled(false);
		Cell cell = mMineField.get(position);
		cell.setDisable(true);

		if (cell.isMine()) {
			v.setBackgroundResource(R.drawable.cell_mine);
		}

		int row = position / mGridSize;
		int column = position % mGridSize;

		Log.d(TAG, "r = " + row + ", c = " + column);
		Toast.makeText(this, "r = " + row + ", c = " + column, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
		Cell cell = mMineField.get(position);
		if (cell.isDisable() && !cell.isFlag()) {
			return true;
		}
		if (cell.isFlag()) {
			v.setBackgroundResource(R.drawable.cell_selector);
			cell.setFlag(false);
		} else {
			v.setBackgroundResource(R.drawable.cell_flag);
			cell.setFlag(true);
		}
		return false;
	}

}
