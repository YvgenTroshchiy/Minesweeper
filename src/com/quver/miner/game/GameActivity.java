package com.quver.miner.game;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.quver.miner.R;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {

	private static final String	TAG			= "myLogs";
	//TODO make choose from list for count
	private GridView			vMineField;

	private int					mGridSize	= 8;
	private int					mCellsCount;
	private int					mMaxMinsCount;
	private ArrayList<Cell>		mMineFieldArray;
	private int					mMinDim;
	private int					mCellSize;
	private MineFieldAdapter	mMineFieldAdapter;

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
				//TODO Make right reset
				recreate();
			}
		});
		;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Cell cell = mMineFieldArray.get(position);

		if (cell.isMine()) {
			v.setBackgroundResource(R.drawable.cell_mine);
			// Game Over
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
		int r = position / mGridSize; //	row
		int c = position % mGridSize; //	column

		switch (getPartOfFild(r, c)) {
		case MIDDLE:
			//	Left Top
			if (mMineFieldArray.get(position - (mGridSize + 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Top
			if (mMineFieldArray.get(position - mGridSize).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right Top
			if (mMineFieldArray.get(position - (mGridSize - 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Left
			if (mMineFieldArray.get(position - 1).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right
			if (mMineFieldArray.get(position + 1).isMine()) {
				cell.incrementSurroundingMines();
			}
			//Left Bottom
			if (mMineFieldArray.get(position + (mGridSize - 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Bottom
			if (mMineFieldArray.get(position + mGridSize).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right Bottom
			if (mMineFieldArray.get(position + (mGridSize + 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			break;
		case TOP:
			//	Left
			if (mMineFieldArray.get(position - 1).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right
			if (mMineFieldArray.get(position + 1).isMine()) {
				cell.incrementSurroundingMines();
			}
			//Left Bottom
			if (mMineFieldArray.get(position + (mGridSize - 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Bottom
			if (mMineFieldArray.get(position + mGridSize).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right Bottom
			if (mMineFieldArray.get(position + (mGridSize + 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			break;
		case BOTTOM:
			//	Left Top
			if (mMineFieldArray.get(position - (mGridSize + 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Top
			if (mMineFieldArray.get(position - mGridSize).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right Top
			if (mMineFieldArray.get(position - (mGridSize - 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Left
			if (mMineFieldArray.get(position - 1).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right
			if (mMineFieldArray.get(position + 1).isMine()) {
				cell.incrementSurroundingMines();
			}
			break;
		case LEFT:
			//	Top
			if (mMineFieldArray.get(position - mGridSize).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right Top
			if (mMineFieldArray.get(position - (mGridSize - 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right
			if (mMineFieldArray.get(position + 1).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Bottom
			if (mMineFieldArray.get(position + mGridSize).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right Bottom
			if (mMineFieldArray.get(position + (mGridSize + 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			break;
		case RIGHT:
			//	Left Top
			if (mMineFieldArray.get(position - (mGridSize + 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Top
			if (mMineFieldArray.get(position - mGridSize).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Left
			if (mMineFieldArray.get(position - 1).isMine()) {
				cell.incrementSurroundingMines();
			}
			//Left Bottom
			if (mMineFieldArray.get(position + (mGridSize - 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Bottom
			if (mMineFieldArray.get(position + mGridSize).isMine()) {
				cell.incrementSurroundingMines();
			}
			break;
		case CORNER_LEFT_TOP:
			//	Right
			if (mMineFieldArray.get(position + 1).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Bottom
			if (mMineFieldArray.get(position + mGridSize).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right Bottom
			if (mMineFieldArray.get(position + (mGridSize + 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			break;
		case CORNER_RIGHT_TOP:
			//	Left
			if (mMineFieldArray.get(position - 1).isMine()) {
				cell.incrementSurroundingMines();
			}
			//Left Bottom
			if (mMineFieldArray.get(position + (mGridSize - 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Bottom
			if (mMineFieldArray.get(position + mGridSize).isMine()) {
				cell.incrementSurroundingMines();
			}
			break;
		case CORNER_LEFT_BOTTOM:
			//	Top
			if (mMineFieldArray.get(position - mGridSize).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right Top
			if (mMineFieldArray.get(position - (mGridSize - 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Right
			if (mMineFieldArray.get(position + 1).isMine()) {
				cell.incrementSurroundingMines();
			}
			break;
		case CORNER_RIGHT_BOTTOM:
			//	Left Top
			if (mMineFieldArray.get(position - (mGridSize + 1)).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Top
			if (mMineFieldArray.get(position - mGridSize).isMine()) {
				cell.incrementSurroundingMines();
			}
			//	Left
			if (mMineFieldArray.get(position - 1).isMine()) {
				cell.incrementSurroundingMines();
			}
			break;
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

	public void openEmptyCells(int position) {
		//TODO maybe return array of position and then make cycle for them and run checkIscellEmpty

		int r = position / mGridSize; //	row
		int c = position % mGridSize; //	column

		switch (getPartOfFild(r, c)) {
		case MIDDLE:
			//	Left Top
			checkIsCellEmpty(position - (mGridSize + 1));
			//	Top
			checkIsCellEmpty(position - mGridSize);
			//	Right Top
			checkIsCellEmpty(position - (mGridSize - 1));
			//	Left
			checkIsCellEmpty(position - 1);
			//	Right
			checkIsCellEmpty(position + 1);
			//Left Bottom
			checkIsCellEmpty(position + (mGridSize - 1));
			//	Bottom
			checkIsCellEmpty(position + mGridSize);
			//	Right Bottom
			checkIsCellEmpty(position + (mGridSize + 1));
			break;
		case TOP:
			//	Left
			checkIsCellEmpty(position - 1);
			//	Right
			checkIsCellEmpty(position + 1);
			//Left Bottom
			checkIsCellEmpty(position + (mGridSize - 1));
			//	Bottom
			checkIsCellEmpty(position + mGridSize);
			//	Right Bottom
			checkIsCellEmpty(position + (mGridSize + 1));
			break;
		case BOTTOM:
			//	Left Top
			checkIsCellEmpty(position - (mGridSize + 1));
			//	Top
			checkIsCellEmpty(position - mGridSize);
			//	Right Top
			checkIsCellEmpty(position - (mGridSize - 1));
			//	Left
			checkIsCellEmpty(position - 1);
			//	Right
			checkIsCellEmpty(position + 1);

			break;
		case LEFT:
			//	Top
			checkIsCellEmpty(position - mGridSize);
			//	Right Top
			checkIsCellEmpty(position - (mGridSize - 1));
			//	Right
			checkIsCellEmpty(position + 1);
			//	Bottom
			checkIsCellEmpty(position + mGridSize);
			//	Right Bottom
			checkIsCellEmpty(position + (mGridSize + 1));
			break;
		case RIGHT:
			//	Left Top
			checkIsCellEmpty(position - (mGridSize + 1));
			//	Top
			checkIsCellEmpty(position - mGridSize);
			//	Left
			checkIsCellEmpty(position - 1);
			//Left Bottom
			checkIsCellEmpty(position + (mGridSize - 1));
			//	Bottom
			checkIsCellEmpty(position + mGridSize);
			break;
		case CORNER_LEFT_TOP:
			//	Right
			checkIsCellEmpty(position + 1);
			//	Bottom
			checkIsCellEmpty(position + mGridSize);
			//	Right Bottom
			checkIsCellEmpty(position + (mGridSize + 1));
			break;
		case CORNER_RIGHT_TOP:
			//	Left
			checkIsCellEmpty(position - 1);
			//Left Bottom
			checkIsCellEmpty(position + (mGridSize - 1));
			//	Bottom
			checkIsCellEmpty(position + mGridSize);
			break;
		case CORNER_LEFT_BOTTOM:
			//	Top
			checkIsCellEmpty(position - mGridSize);
			//	Right Top
			checkIsCellEmpty(position - (mGridSize - 1));
			//	Right
			checkIsCellEmpty(position + 1);
			break;
		case CORNER_RIGHT_BOTTOM:
			//	Left Top
			checkIsCellEmpty(position - (mGridSize + 1));
			//	Top
			checkIsCellEmpty(position - mGridSize);
			//	Left
			checkIsCellEmpty(position - 1);
			break;
		}
	}

	public void setCountOfSurruondMinesToView(View v, int count, int position) {
		mMineFieldArray.get(position).setClicked(true);
		mMineFieldArray.get(position).enableForLongClick(false);

		switch (count) {
		case 0:
			v.setBackgroundResource(R.drawable.cell_0);
			openEmptyCells(position);
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
		//	TODO Try another way.
		//	Do changes in adapter and then refresh all gridView adapter.notifyDataChanged();

		Cell cell;
		for (int i = 0; i < mMineFieldArray.size(); i++) {
			cell = mMineFieldArray.get(i);
			if (cell.isMine()) {
				vMineField.getChildAt(i).setBackgroundResource(R.drawable.cell_mine);
			}
			if (cell.isFlag() && !cell.isMine()) {
				vMineField.getChildAt(i).setBackgroundResource(R.drawable.cell_flag_wrong);
			}
		}
	}

}
