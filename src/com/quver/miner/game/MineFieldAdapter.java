package com.quver.miner.game;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

import com.quver.miner.R;

public class MineFieldAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Cell> mMineField;
    private int mCellSize;

    public MineFieldAdapter(Context c, ArrayList<Cell> mineField, int cellSize) {
        mContext = c;
        mCellSize = cellSize;
        mMineField = mineField;
    }

    @Override
    public int getCount() {
        return mMineField.size();
    }

    @Override
    public Object getItem(int position) {
        return mMineField.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(mCellSize, mCellSize));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setBackgroundResource(R.drawable.cell_selector);
        return imageView;
    }
}
