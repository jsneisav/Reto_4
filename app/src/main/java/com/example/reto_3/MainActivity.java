package com.example.reto_3;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private boolean mGameOver;

    private TicTacToeGame mGame;

    // Various text displayed
    private TextView mInfoTextView;
    private TextView mHw;
    private TextView mT;
    private TextView mCw;
    private BoardView mBoardView;
    private int mHumanw = 0;
    private int mTie = 0;
    private int mComputerw = 0;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInfoTextView = (TextView) findViewById(R.id.information);
        mHw = (TextView) findViewById(R.id.textViewH);
        mT = (TextView) findViewById(R.id.textViewT);
        mCw = (TextView) findViewById(R.id.textViewC);
        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);
        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);
        displayScore();
        startNewGame();


    }

    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.ai_difficulty:
                showDialog(DIALOG_DIFFICULTY_ID);
                return true;
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
        }
        return false;
    }

    private void startNewGame() {
        mGameOver = false;
        mGame.clearBoard();
        mBoardView.invalidate();
        // Human goes first
        mInfoTextView.setText(R.string.first_human);
    } // End of startNewGame

    // Listen for touches on the board
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
    // Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;
            if (!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos)){
                setMove(TicTacToeGame.HUMAN_PLAYER, pos);
                // If no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer);
                    mComputerMediaPlayer.start();
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();
                }

                if (winner == 0)
                    mInfoTextView.setText(R.string.turn_human);
                else if (winner == 1) {
                    mInfoTextView.setText(R.string.result_tie);
                    mTie++;
                    mGameOver = true;
                }
                else if (winner == 2) {
                    mInfoTextView.setText(R.string.result_human_wins);
                    mHumanw++;
                    mVictoria.start();
                    mGameOver = true;
                }
                else if (winner == 3){
                    mInfoTextView.setText(R.string.result_computer_wins);
                    mComputerw++;
                    mGameOver = true;
                }
                displayScore();
            }
    // So we aren't notified of continued events when finger is moved
            return false;
        }
    };

    private void displayScore(){
        mHw.setText("Human: \n"+mHumanw);
        mT.setText("Tie: \n"+mTie);
        mCw.setText("Computer: \n"+mComputerw);
    }

    private boolean setMove(char player, int location) {
        if (mGame.setMove(player, location)) {
            mBoardView.invalidate(); // Redraw the board
            mHumanMediaPlayer.start();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id) {
            case DIALOG_DIFFICULTY_ID:
                builder.setTitle(R.string.difficulty_choose);
                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert)};

                // selected is the radio button that should be selected.
                int selected = 1;
                builder.setSingleChoiceItems(levels, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss(); // Close dialog

                                if (levels[item].equals(getResources().getString(R.string.difficulty_easy))){
                                    mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                                }
                                else if (levels[item].equals(getResources().getString(R.string.difficulty_harder))){
                                    mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                                }
                                else
                                    mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);

                                // Display the selected difficulty level
                                Toast.makeText(getApplicationContext(), levels[item],
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog = builder.create();

                break;
            case DIALOG_QUIT_ID:
                // Create the quit confirmation dialog

                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();

                break;
        }
        return dialog;
    }

    // sounds

    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;
    MediaPlayer mVictoria;
    @Override
    protected void onResume() {
        super.onResume();
        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.user);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.ia);
        mVictoria = MediaPlayer.create(getApplicationContext(), R.raw.victoria);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
        mVictoria.release();
    }

};



