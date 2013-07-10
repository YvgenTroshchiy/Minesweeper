package com.quver.miner.game;

public class Cell {

	private int		surroundingMines	= 0;
	private boolean	isMine				= false;
	private boolean	isFlag				= false;
	private boolean	enableForLongClick	= true;

	public int getSurroundingMines() {
		return surroundingMines;
	}

	public void incrementSurroundingMines() {
		this.surroundingMines++;
	}

	public boolean isMine() {
		return isMine;
	}

	public void setMine(boolean isMine) {
		this.isMine = isMine;
	}

	public boolean isFlag() {
		return isFlag;
	}

	public void setFlag(boolean isFlag) {
		this.isFlag = isFlag;
	}

	public boolean isEnableForLongClick() {
		return enableForLongClick;
	}

	public void enableForLongClick(boolean isDisable) {
		this.enableForLongClick = isDisable;
	}

}
