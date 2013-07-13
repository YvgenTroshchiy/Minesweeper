package com.quver.miner.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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

public class SingleGameActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {

	private static final String	TAG				= "GameActivity";

	public static final int		VIBRATE_TIME	= 60;
	//TODO make choose from list for count
	private GridView			vMineField;

	private int					mGridSize		= 8;
	private int					mCellsCount;
	private int					mMaxMinsCount;
	private ArrayList<Cell>		mMineFieldArray;
	private int					mMinDim;
	private int					mCellSize;
	private MineFieldAdapter	mMineFieldAdapter;
	private Vibrator			mVibrator;

	private enum PartOfFild {
		MIDDLE, TOP, BOTTOM, LEFT, RIGHT, CORNER_LEFT_TOP, CORNER_RIGHT_TOP, CORNER_LEFT_BOTTOM, CORNER_RIGHT_BOTTOM
	};

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

		mMineFieldArray = new ArrayList<Cell>(mCellsCount);
		for (int i = 0; i < mCellsCount; i++) {
			mMineFieldArray.add(new Cell());
		}

		mMineFieldAdapter = new MineFieldAdapter(this, mMineFieldArray, mCellSize);
		vMineField.setAdapter(mMineFieldAdapter);
		vMineField.setOnItemClickListener(this);
		vMineField.setOnItemLongClickListener(this);

		setRandomMines();

		((Button) findViewById(R.id.btn_resetGame)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
		Cell cell = mMineFieldArray.get(position);

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
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
		mVibrator.vibrate(VIBRATE_TIME);

		Cell cell = mMineFieldArray.get(position);

		if (!cell.isEnableForLongClick() && !cell.isFlag()) {
			return true;
		}
		if (cell.isEnableForLongClick() && cell.getSurroundingMines() > 0) {
			//TODO highlight surround cells what not now open
		}

		//	Turn on off flag
		if (cell.isFlag()) {
			v.setBackgroundResource(R.drawable.cell_selector);
			cell.setFlag(false);
		} else {
			v.setBackgroundResource(R.drawable.cell_flag);
			cell.setFlag(true);
		}
		return false;
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
			if (mMineFieldArray.get(p).isMine()) {
				cell.incrementSurroundingMines();
			}
		}

		return cell.getSurroundingMines();
	}

	private void checkIsCellEmpty(int position) {
		Cell cell = mMineFieldArray.get(position);

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
		mMineFieldArray.get(position).setClicked(true);
		mMineFieldArray.get(position).enableForLongClick(false);

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
	}

	private void setRandomMines() {
		Random r = new Random();
		//	This is number of mines what need in TZ
		//		int minesCount = mMaxMinsCount / 2 + r.nextInt(mMaxMinsCount / 2);

		//	This is better number of mines for playing
		int minesCount = mMaxMinsCount / 4 + r.nextInt(mMaxMinsCount / 4);

		for (int i = 0; i < minesCount; i++) {
			int p = r.nextInt(mCellsCount);
			mMineFieldArray.get(p).setMine(true);
		}
	}

	public void showAllMines() {
		//TODO Try another way.
		//	Do changes in adapter and then refresh all gridView adapter.notifyDataChanged();

		Cell cell;
		for (int i = 0; i < mMineFieldArray.size(); i++) {
			cell = mMineFieldArray.get(i);
			if (cell.isMine() && !cell.isFlag()) {
				vMineField.getChildAt(i).setBackgroundResource(R.drawable.cell_mine);
			}
			if (!cell.isMine() && cell.isFlag()) {
				vMineField.getChildAt(i).setBackgroundResource(R.drawable.cell_flag_wrong);
			}
		}
	}

}
