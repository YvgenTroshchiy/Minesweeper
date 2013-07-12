package com.quver.miner.game;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.quver.miner.R;

public class MineFieldAdapter extends BaseAdapter {

	private Context			mContext;
	private ArrayList<Cell>	mMineFieldArray;
	private int				mCellSize;

	public MineFieldAdapter(Context c, ArrayList<Cell> mineFieldArray, int cellSize) {
		mContext = c;
		mCellSize = cellSize;
		mMineFieldArray = mineFieldArray;
	}

	@Override
	public int getCount() {
		return mMineFieldArray.size();
	}

	@Override
	public Object getItem(int position) {
		return mMineFieldArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//	TODO remade LayoutInflater in getView and add in him textView. TextView.setText/setColor

		ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(mCellSize, mCellSize));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(0, 0, 0, 0);
		} else {
			imageView = (ImageView) convertView;
		}

		if (mMineFieldArray.get(position).isFlag()) {
			imageView.setBackgroundResource(R.drawable.cell_mine);
		} else {
			imageView.setBackgroundResource(R.drawable.cell_selector);
		}
		return imageView;
	}
}
