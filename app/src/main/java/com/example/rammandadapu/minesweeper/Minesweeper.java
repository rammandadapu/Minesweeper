package com.example.rammandadapu.minesweeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class Minesweeper extends AppCompatActivity {

    int totalRows = 12;
    int totalCols = 8;
    int totalMines = 30;

    int tilePadding = 1;
    Tile[][] tiles;
    Tile[][] tilesCopy;
    Handler timer = new Handler();
    int secondsPassed = 0;
    boolean timerStarted = false;
    int correctFalgs = 0;
    SharedPreferences preferences;
    boolean isAlive = true;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("secondsPassed", secondsPassed);
        outState.putBoolean("isAlive", isAlive);
        outState.putSerializable("tiles", tiles);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Toast.makeText(Minesweeper.this, "onCreate", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minesweeper);

        if (null != savedInstanceState) {
            restore(savedInstanceState);
        }
        initialize();
        setResetButton();

    }

    protected void restore(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isAlive = savedInstanceState.getBoolean("isAlive");
        secondsPassed = savedInstanceState.getInt("secondsPassed");
        TextView timerText = (TextView) findViewById(R.id.Timer);
        String curTime = Integer.toString(secondsPassed);

        if (secondsPassed < 10) {
            timerText.setText("00" + curTime);
        } else if (secondsPassed < 100) {
            timerText.setText("0" + curTime);
        } else {
            timerText.setText(curTime);
        }
        if (secondsPassed > 0 && isAlive) {
            timer.removeCallbacks(updateTimer);
            timer.postDelayed(updateTimer, 1000);
        }

        tilesCopy = (Tile[][]) savedInstanceState.getSerializable("tiles");

    }

    void initialize() {
        TableLayout mineField = (TableLayout) findViewById(R.id.MineField);
        mineField.removeAllViews();
        tiles = new Tile[totalRows][totalCols];
        if (null == tilesCopy) {
            showGameBoard();
        } else {
            repaint();
            tilesCopy = null;
        }
        ImageButton imageButton = (ImageButton) findViewById(R.id.Smiley);
        if (isAlive)
            imageButton.setBackgroundResource(R.mipmap.happy);
        else
            imageButton.setBackgroundResource(R.mipmap.sad);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_minesweeper, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void setResetButton() {
        Button button = (Button) findViewById(R.id.Reset);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAlive = true;
                tiles = null;
                initialize();
                secondsPassed = 0;
                stopTimer();
                ((TextView) findViewById(R.id.Timer)).setText("000");
                Toast.makeText(Minesweeper.this, "Re-started", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void repaint() {

        for (int row = 0; row < totalRows; row++) {
            TableRow tableRow = new TableRow(this);
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();  // deprecated
            int height = display.getHeight();  // deprecated
            tableRow.setLayoutParams(new LayoutParams(width / 12, height / 18));
            //for every column
            for (int col = 0; col < totalCols; col++) {
                tiles[row][col] = new Tile(this);
                tiles[row][col].setDefaults();
                tiles[row][col].setMine(tilesCopy[row][col].isMine());
                tiles[row][col].setFlag(tilesCopy[row][col].isFlag());
                if (!tilesCopy[row][col].isMine())
                    tiles[row][col].setNoSurroundingMines(tilesCopy[row][col].getNoSurroundingMines());
                if (!tilesCopy[row][col].isCovered() && !tilesCopy[row][col].isMine()) {
                    tiles[row][col].showNumber();
                    tiles[row][col].setUncovered();
                }
                if (!tilesCopy[row][col].isCovered() && tilesCopy[row][col].isMine()) {
                    tiles[row][col].setText("M");
                    tiles[row][col].setUncovered();
                }

                if (tilesCopy[row][col].isFlag())
                    tiles[row][col].setFlag(true);

                tiles[row][col].setLayoutParams(new LayoutParams(width / 12, height / 18));
                //add some padding to the tile
                tiles[row][col].setPadding(tilePadding, tilePadding, tilePadding, tilePadding);

                final int curRow = row;
                final int curCol = col;


                //add a click listener
                tiles[row][col].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startTimer();
                        if (!tiles[curRow][curCol].isFlag()) {
                            if (tiles[curRow][curCol].isMine()) {
                                loseGame();
                            } else if (wonGame()) {
                                winGame();
                            } else {
                                uncoverTiles(curRow, curCol);
                            }

                        }
                    }
                });

                //add a long click listener
                final int finalRow = row;
                final int finalCol = col;
                tiles[row][col].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if(tiles[finalRow][finalCol].isCovered()) {
                            if (tiles[finalRow][finalCol].isMine() && !tiles[finalRow][finalCol].isFlag())
                                correctFalgs++;

                            tiles[finalRow][finalCol].setFlag(!tiles[finalRow][finalCol].isFlag());
                        }
                        return true;
                    }
                });


                tableRow.addView(tiles[row][col]);

            }

            TableLayout mineField = (TableLayout) findViewById(R.id.MineField);
            mineField.addView(tableRow, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height / 18));
        }


    }

    void showGameBoard() {


        for (int row = 0; row < totalRows; row++) {
            TableRow tableRow = new TableRow(this);
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();  // deprecated
            int height = display.getHeight();  // deprecated
            tableRow.setLayoutParams(new LayoutParams(width / 12, height / 18));
            //for every column
            for (int col = 0; col < totalCols; col++) {
                //create a tile
                if (null == tiles[row][col]) {
                    tiles[row][col] = new Tile(this);
                    //set the tile defaults
                    tiles[row][col].setDefaults();
                } else {
                    if (!tiles[row][col].isCovered())
                        tiles[row][col].openTile();

                    if (tiles[row][col].isFlag())
                        tiles[row][col].setFlag(true);
                }
                tiles[row][col].setLayoutParams(new LayoutParams(width / 12, height / 18));
                tiles[row][col].setPadding(tilePadding, tilePadding, tilePadding, tilePadding);


                final int curRow = row;
                final int curCol = col;

                tiles[row][col].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startTimer();
                        if (!tiles[curRow][curCol].isFlag()) {
                            if (tiles[curRow][curCol].isMine()) {
                                loseGame();
                            } else if (wonGame()) {
                                winGame();
                            } else {
                                uncoverTiles(curRow, curCol);
                            }

                        }
                    }
                });

                final int finalRow = row;
                final int finalCol = col;
                tiles[row][col].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if(tiles[finalRow][finalCol].isCovered()) {
                            if (tiles[finalRow][finalCol].isMine() && !tiles[finalRow][finalCol].isFlag())
                                correctFalgs++;

                            tiles[finalRow][finalCol].setFlag(!tiles[finalRow][finalCol].isFlag());
                        }
                        return true;
                    }
                });


                //add the tile to the table row
                tableRow.addView(tiles[row][col]);

            }

            //add the row to the minefield layout
            TableLayout mineField = (TableLayout) findViewById(R.id.MineField);
            mineField.addView(tableRow, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height / 18));
        }
        setupMineField();
    }


    public void setupMineField() {
        Random random = new Random();
        int mineRow;
        int mineCol;
        for (int i = 0; i < totalMines; i++) {
            mineRow = random.nextInt(totalRows);
            mineCol = random.nextInt(totalCols);


            if (tiles[mineRow][mineCol].isMine()) //already a mine
                i--;
            else {
                tiles[mineRow][mineCol].plantMine();
                int startRow = mineRow - 1;
                int startCol = mineCol - 1;
                int checkRows = 3;
                int checkCols = 3;
                if (startRow < 0) //if it is on the first row
                {
                    startRow = 0;
                    checkRows = 2;
                } else if (startRow + 3 > totalRows) //if it is on the last row
                    checkRows = 2;

                if (startCol < 0) {
                    startCol = 0;
                    checkCols = 2;
                } else if (startCol + 3 > totalCols) //if it is on the last row
                    checkCols = 2;

                for (int j = startRow; j < startRow + checkRows; j++) //3 rows across
                {
                    for (int k = startCol; k < startCol + checkCols; k++) //3 rows down
                    {
                        if (!tiles[j][k].isMine()) //if it isn't a mine
                            tiles[j][k].updateSurroundingMineCount();
                    }
                }

            }
        }
    }

    void loseGame() {
        isAlive = false;
        stopTimer();
        ImageButton imageButton = (ImageButton) findViewById(R.id.Smiley);
        imageButton.setBackgroundResource(R.mipmap.sad);

        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < totalCols; j++) {
                //if the tile is covered
                if (tiles[i][j].isCovered()) {
                    //if there is no flag or mine
                    if (!tiles[i][j].isFlag() && !tiles[i][j].isMine()) {
                        // tiles[i][j].openTile();
                    }
                    //if there is a mine but no flag
                    else if (tiles[i][j].isMine() && !tiles[i][j].isFlag()) {
                        tiles[i][j].openTile();
                    }

                }
            }
        }
        promptUser();
    }

    void promptUser() {
        new AlertDialog.Builder(this)
                .setTitle("You Lost ")
                .setMessage("Do you want to restart Game?")
                .setIcon(R.mipmap.sad)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        isAlive = true;
                        initialize();
                        secondsPassed = 0;
                        ((TextView) findViewById(R.id.Timer)).setText("000");
                        Toast.makeText(Minesweeper.this, "Re-Started", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    boolean wonGame() {
        return correctFalgs == totalMines;
    }

    void winGame() {
        Toast.makeText(Minesweeper.this, "You Won", Toast.LENGTH_SHORT).show();
        stopTimer();
    }

    void uncoverTiles(int row, int col) {

        //if the tile is a mine, or a flag return
        if (tiles[row][col].isMine() || tiles[row][col].isFlag())
            return;

        tiles[row][col].openTile();

        if (tiles[row][col].getNoSurroundingMines() > 0)
            return;

        //go one row and col back
        int startRow = row - 1;
        int startCol = col - 1;
        //check 3 rows across and 3 down
        int checkRows = 3;
        int checkCols = 3;
        if (startRow < 0) //if it is on the first row
        {
            startRow = 0;
            checkRows = 2;
        } else if (startRow + 3 > totalRows) //if it is on the last row
            checkRows = 2;

        if (startCol < 0) {
            startCol = 0;
            checkCols = 2;
        } else if (startCol + 3 > totalCols) //if it is on the last row
            checkCols = 2;

        for (int i = startRow; i < startRow + checkRows; i++) //3 or 2 rows across
        {
            for (int j = startCol; j < startCol + checkCols; j++) //3 or 2 rows down
            {
                if (tiles[i][j].isCovered())
                    uncoverTiles(i, j);
            }
        }
    }

    public void endGame() {
        //imageButton.setBackgroundResource(R.drawable.smile);
        TableLayout mineField = (TableLayout) findViewById(R.id.MineField);
        // remove the table rows from the minefield table layout
        mineField.removeAllViews();

        // reset variables
        timerStarted = false;

    }

    public void startTimer() {
        if (secondsPassed == 0) {
            timer.removeCallbacks(updateTimer);
            timer.postDelayed(updateTimer, 1000);
        }
    }

    public void stopTimer() {
        timer.removeCallbacks(updateTimer);
    }

    private Runnable updateTimer = new Runnable() {

        public void run() {
            TextView timerText = (TextView) findViewById(R.id.Timer);
            long currentMilliseconds = System.currentTimeMillis();
            ++secondsPassed;
            String curTime = Integer.toString(secondsPassed);
            //update the text view
            if (secondsPassed < 10) {
                timerText.setText("00" + curTime);
            } else if (secondsPassed < 100) {
                timerText.setText("0" + curTime);
            } else {
                timerText.setText(curTime);
            }
            timer.postAtTime(this, currentMilliseconds);
            //run again in 1 second
            timer.postDelayed(updateTimer, 1000);
        }
    };
}
