package com.quver.miner.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.quver.miner.R;
import com.quver.miner.game.Cell;
import com.quver.miner.game.MineFieldAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class NetworkGameActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {
	private static final String	TAG				= "GameActivity";
	
	//TODO make game glass
	public static final int		VIBRATE_TIME	= 60;
	//TODO make choose from list for count
	private GridView			vMineField;
	
	private int					mGridSize;
	private int					mCellsCount;
	private int					mOpenedCells	= 0;
	private int					mMinDim;
	private int					mCellSize;
	private ArrayList<Cell>		mCellsArray;
	private ArrayList<Integer>	mMinesPosition	= new ArrayList<Integer>();
	private LinkedList<Integer>	mFlagsPosition	= new LinkedList<Integer>();
	private MineFieldAdapter	mMineFieldAdapter;
	private Vibrator			mVibrator;

	private enum PartOfFild {
		MIDDLE, TOP, BOTTOM, LEFT, RIGHT, CORNER_LEFT_TOP, CORNER_RIGHT_TOP, CORNER_LEFT_BOTTOM, CORNER_RIGHT_BOTTOM
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_field);
		
		Intent intent = getIntent();
		mGridSize = intent.getIntExtra(GameSettings.GRID_SIZE, 8);
		mMinesPosition = intent.getIntegerArrayListExtra(GameSettings.MINES_ARRAY);
		
		mCellsCount = mGridSize * mGridSize;
		
		Display display = getWindowManager().getDefaultDisplay();
		//TODO convert px to dp
		int width = display.getWidth();
		int height = display.getHeight();
		
		mMinDim = Math.min(width, height);
		//TODO convert px to dp
		mMinDim -= getResources().getDimension(R.dimen.activity_horizontal_margin);
		mCellSize = mMinDim / mGridSize;
		
		vMineField = (GridView) findViewById(R.id.mineField);
		vMineField.setNumColumns(mGridSize);
		
		mCellsArray = new ArrayList<Cell>(mCellsCount);
		for (int i = 0; i < mCellsCount; i++) {
			mCellsArray.add(new Cell());
		}
		
		mMineFieldAdapter = new MineFieldAdapter(this, mCellsArray, mCellSize);
		vMineField.setAdapter(mMineFieldAdapter);
		vMineField.setOnItemClickListener(this);
		vMineField.setOnItemLongClickListener(this);
		
		setMines();
		
		Button vBtnField = (Button) findViewById(R.id.btn_field);
		vBtnField.setText(getString(R.string.restart));
		vBtnField.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mOpenedCells = 0;
				//TODO Recreate adapter and GridView
				recreate();
				//				mMineFieldAdapter.notifyDataSetChanged();
				//				vMineField.invalidateViews();
				//				vMineField.setAdapter(mMineFieldAdapter);
			}
		});
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Cell cell = mCellsArray.get(position);
		
		if (cell.isMine()) {
			// Game Over
			mVibrator.vibrate(VIBRATE_TIME);
			vMineField.setEnabled(false);
			v.setBackgroundResource(R.drawable.cell_mine);
			showAllMines();
			return;
		}
		
		if (cell.isClicked() || !cell.isEnableForLongClick() || cell.isFlag()) {
			//	We can remove flag only in long click. Just for usability.
			return;
		}
		
		v.setEnabled(false);
		//		v.setClickable(false);
		//		v.setLongClickable(false);
		cell.enableForLongClick(false);//	Because v.setLongClickable(false); does not work
		
		setCountOfSurruondMinesToView(v, getNumberOfSurroundingMines(cell, position), position);
		isGameOver();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
		mVibrator.vibrate(VIBRATE_TIME);
		
		Cell cell = mCellsArray.get(position);
		
		if (!cell.isEnableForLongClick() && !cell.isFlag()) { return true; }
		if (cell.isEnableForLongClick() && cell.getSurroundingMines() > 0) {
			//TODO highlight surround cells what not now open
		}
		
		//	Turn on off flag
		if (cell.isFlag()) {
			v.setBackgroundResource(R.drawable.cell_selector);
			cell.setFlag(false);
			remooveValueFromFlagPosition(position);
		} else {
			v.setBackgroundResource(R.drawable.cell_flag);
			cell.setFlag(true);
			mFlagsPosition.add(position);
		}
		return false;
	}
	
	private void remooveValueFromFlagPosition(Integer positionToRemoove) {
		for (Iterator<Integer> i = mFlagsPosition.iterator(); i.hasNext();) {
			Integer integer = (Integer) i.next();
			if (integer == positionToRemoove) {
				i.remove();
			}
		}
	}

	public PartOfFild getPartOfFild(int r, int c) {
		//	Middle part of the Field
		if ((r != 0) && (r != mGridSize - 1) && (c != 0) && (c != mGridSize - 1)) {
			return PartOfFild.MIDDLE;
		}
		//	Top part of the Field (without corners)
		if ((r == 0) && (c != 0) && (c != mGridSize - 1)) {
			return PartOfFild.TOP;
		}
		//	Bottom part of the Field (without corners)
		if ((r == mGridSize - 1) && (c != 0) && (c != mGridSize - 1)) {
			return PartOfFild.BOTTOM;
		}
		//	Left part of the Field (without corners)
		if ((c == 0) && (r != 0) && (r != mGridSize - 1)) {
			return PartOfFild.LEFT;
		}
		//	Right part of the Field (without corners)
		if ((c == mGridSize - 1) && (r != 0) && (r != mGridSize - 1)) {
			return PartOfFild.RIGHT;
		}

		//	Corners:
		// Left Top
		if ((r == 0) && (c == 0)) {
			return PartOfFild.CORNER_LEFT_TOP;
		}
		//	Right Top
		if ((r == 0) && (c == mGridSize - 1)) {
			return PartOfFild.CORNER_RIGHT_TOP;
		}
		// Left Bottom
		if ((r == mGridSize - 1) && (c == 0)) {
			return PartOfFild.CORNER_LEFT_BOTTOM;
		}
		//	Right Bottom
		if ((r == mGridSize - 1) && (c == mGridSize - 1)) {
			return PartOfFild.CORNER_RIGHT_BOTTOM;
		}
		return null;
	}

	public int getNumberOfSurroundingMines(Cell cell, int position) {
		for (int p : getPositionsOfSurroundCells(position)) {
			if (mCellsArray.get(p).isMine()) {
				cell.incrementSurroundingMines();
			}
		}

		return cell.getSurroundingMines();
	}

	private void checkIsCellEmpty(int position) {
		Cell cell = mCellsArray.get(position);

		if (cell.isClicked()) {
			return;
		}

		View v = vMineField.getChildAt(position);
		setCountOfSurruondMinesToView(v, getNumberOfSurroundingMines(cell, position), position);
	}

	public LinkedList<Integer> getPositionsOfSurroundCells(int position) {
		LinkedList<Integer> listOfPositions = new LinkedList<Integer>();

		int r = position / mGridSize; //	row
		int c = position % mGridSize; //	column

		switch (getPartOfFild(r, c)) {
		case MIDDLE:
			//	Left Top
			listOfPositions.add(position - (mGridSize + 1));
			//	Top
			listOfPositions.add(position - mGridSize);
			//	Right Top
			listOfPositions.add(position - (mGridSize - 1));
			//	Left
			listOfPositions.add(position - 1);
			//	Right
			listOfPositions.add(position + 1);
			//	Left Bottom
			listOfPositions.add(position + (mGridSize - 1));
			//	Bottom
			listOfPositions.add(position + mGridSize);
			//	Right Bottom
			listOfPositions.add(position + (mGridSize + 1));
			break;
		case TOP:
			//	Left
			listOfPositions.add(position - 1);
			//	Right
			listOfPositions.add(position + 1);
			//	Left Bottom
			listOfPositions.add(position + (mGridSize - 1));
			//	Bottom
			listOfPositions.add(position + mGridSize);
			//	Right Bottom
			listOfPositions.add(position + (mGridSize + 1));
			break;
		case BOTTOM:
			//	Left Top
			listOfPositions.add(position - (mGridSize + 1));
			//	Top
			listOfPositions.add(position - mGridSize);
			//	Right Top
			listOfPositions.add(position - (mGridSize - 1));
			//	Left
			listOfPositions.add(position - 1);
			//	Right
			listOfPositions.add(position + 1);

			break;
		case LEFT:
			//	Top
			listOfPositions.add(position - mGridSize);
			//	Right Top
			listOfPositions.add(position - (mGridSize - 1));
			//	Right
			listOfPositions.add(position + 1);
			//	Bottom
			listOfPositions.add(position + mGridSize);
			//	Right Bottom
			listOfPositions.add(position + (mGridSize + 1));
			break;
		case RIGHT:
			//	Left Top
			listOfPositions.add(position - (mGridSize + 1));
			//	Top
			listOfPositions.add(position - mGridSize);
			//	Left
			listOfPositions.add(position - 1);
			//	Left Bottom
			listOfPositions.add(position + (mGridSize - 1));
			//	Bottom
			listOfPositions.add(position + mGridSize);
			break;
		case CORNER_LEFT_TOP:
			//	Right
			listOfPositions.add(position + 1);
			//	Bottom
			listOfPositions.add(position + mGridSize);
			//	Right Bottom
			listOfPositions.add(position + (mGridSize + 1));
			break;
		case CORNER_RIGHT_TOP:
			//	Left
			listOfPositions.add(position - 1);
			//	Left Bottom
			listOfPositions.add(position + (mGridSize - 1));
			//	Bottom
			listOfPositions.add(position + mGridSize);
			break;
		case CORNER_LEFT_BOTTOM:
			//	Top
			listOfPositions.add(position - mGridSize);
			//	Right Top
			listOfPositions.add(position - (mGridSize - 1));
			//	Right
			listOfPositions.add(position + 1);
			break;
		case CORNER_RIGHT_BOTTOM:
			//	Left Top
			listOfPositions.add(position - (mGridSize + 1));
			//	Top
			listOfPositions.add(position - mGridSize);
			//	Left
			listOfPositions.add(position - 1);
			break;
		}
		return listOfPositions;
	}

	public void setCountOfSurruondMinesToView(View v, int count, int position) {
		mCellsArray.get(position).setClicked(true);
		mCellsArray.get(position).enableForLongClick(false);
		mOpenedCells++;
		
		switch (count) {
		case 0:
			v.setBackgroundResource(R.drawable.cell_0);
			//Check if empty cell surround
			for (int p : getPositionsOfSurroundCells(position)) {
				checkIsCellEmpty(p);
			}
			break;
		case 1:
			v.setBackgroundResource(R.drawable.cell_1);
			break;
		case 2:
			v.setBackgroundResource(R.drawable.cell_2);
			break;
		case 3:
			v.setBackgroundResource(R.drawable.cell_3);
			break;
		case 4:
			v.setBackgroundResource(R.drawable.cell_4);
			break;
		case 5:
			v.setBackgroundResource(R.drawable.cell_5);
			break;
		case 6:
			v.setBackgroundResource(R.drawable.cell_6);
			break;
		case 7:
			v.setBackgroundResource(R.drawable.cell_7);
			break;
		case 8:
			v.setBackgroundResource(R.drawable.cell_8);
			break;
		}
		isGameOver();
	}

	private void setMines() {
		for (int  position : mMinesPosition) {
			mCellsArray.get(position).setMine(true);
		}
	}

	public void showAllMines() {
		Cell cell;
		
		for (Integer position : mMinesPosition) {
			cell = mCellsArray.get(position);
			if (!cell.isFlag()) {
				vMineField.getChildAt(position).setBackgroundResource(R.drawable.cell_mine);
			}
		}
		
		for (Integer position : mFlagsPosition) {
			cell = mCellsArray.get(position);
			if (!cell.isMine()) {
				vMineField.getChildAt(position).setBackgroundResource(R.drawable.cell_flag_wrong);
			}
		}
	}
	
	public void isGameOver() {
		if (mCellsCount - mOpenedCells == mMinesPosition.size()) {
			vMineField.setEnabled(false);
			Toast toast = Toast.makeText(this, getResources().getString(R.string.win), Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			
			//TODO send message via bluetooth
		}
	}
	
}
