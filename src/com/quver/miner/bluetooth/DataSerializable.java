package com.quver.miner.bluetooth;

import java.io.Serializable;

public class DataSerializable implements Serializable {
	private static final long serialVersionUID = 1L;
	private int mGridSize;
	private int[] mMinesPosition;
	private Boolean mIsWin = null;
	
	public DataSerializable(int gridSize, int[] minesPosition) {
		mGridSize = gridSize;
		mMinesPosition = minesPosition;
	}
	
	public DataSerializable(Boolean isWin) {
		mIsWin = isWin;
	}
	
	public int getGridSize() {
		return mGridSize;
	}
	
	public int[] getMinesPosition() {
		return mMinesPosition;
	}
	
	public Boolean isWin() {
		return mIsWin;
	}
	
}
