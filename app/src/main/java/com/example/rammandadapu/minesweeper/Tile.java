package com.example.rammandadapu.minesweeper;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.Button;

import java.io.Serializable;

/**
 * Created by Ram Mandadapu on 2/27/2016.
 */
public class Tile extends Button implements Serializable {
    private boolean isMine;
    private boolean isFlag;
    private boolean isCovered;
    private int noSurroundingMines = 0;
    private int color[] = {Color.BLACK, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.RED, Color.YELLOW, Color.DKGRAY, Color.rgb(10, 10, 10), Color.rgb(50, 20, 55)};

    public Tile(Context context) {
        super(context);
    }

    public Tile(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Tile(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDefaults() {
        isMine = false;
        isFlag = false;
        isCovered = true;
        noSurroundingMines = 0;

        this.setBackgroundResource(R.mipmap.tile);
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public void setFlag(boolean flag) {
        this.isFlag = flag;
        if (isFlag)
            this.setText("F");
        else
            this.setText("");


    }

    public void setFlagIcon() {
        this.setText("F");
    }

    public void setQuestionMarkIcon() {
        this.setText("?");
    }

    public boolean isCovered() {
        return isCovered;
    }

    //uncover the tile
    public void openTile() {
        if (!isCovered)
            return;

        setUncovered();
        if (this.isMine())
            triggerMine();
        else
            showNumber();
    }

    public void setUncovered() {
        isCovered = false;
    }


    public void showNumber() {
        if (0 != noSurroundingMines) {
            this.setText(Integer.toString(noSurroundingMines));
            this.setTextColor(color[noSurroundingMines]);
        }
    }


    public void updateSurroundingMineCount() {
        noSurroundingMines++;
    }


    public void plantMine() {
        isMine = true;
    }

    public void triggerMine() {
        this.setText("M");
    }

    public boolean isMine() {

        return isMine;
    }

    public boolean isFlag() {
        return isFlag;
    }

    public int getNoSurroundingMines() {
        return noSurroundingMines;
    }

    public void setNoSurroundingMines(int n) {
        noSurroundingMines = n;
    }

}

