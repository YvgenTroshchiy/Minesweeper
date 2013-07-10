package com.quver.miner.game;

public class Cell {

	private int		surroundingMines	= 0;
	private boolean	isMine				= false;
	private boolean	isFlag				= false;
	private boolean	isDisable			= false;

	public int getSurroundingMines() {
		return surroundingMines;
	}

	public void setSurroundingMines(int surroundingMines) {
		this.surroundingMines = surroundingMines;
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

	public boolean isDisable() {
		return isDisable;
	}

	public void setDisable(boolean isDisable) {
		this.isDisable = isDisable;
	}

}
