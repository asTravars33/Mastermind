package com.wangjessica.jwlab11b;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.HashMap;

public class GeneticAlgActivity extends AppCompatActivity implements MastermindView.GameOverHandler, MastermindView.ClueReceivedHandler{

    // Layout components
    MastermindView gameView;
    ImageView[] colorOpts = new ImageView[6];
    TextView gameOverText;
    Button resetButton;

    // State variables
    boolean running = true;
    private int curLayer = 7;
    int[] recentClue;
    String prevGuesses = "";
    String guess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mastermind);

        // Instantiate layout components
        gameOverText = findViewById(R.id.game_over_msg);
        resetButton = findViewById(R.id.restart);
        gameView = findViewById(R.id.game_view);
        LinearLayout layout = findViewById(R.id.choices);

        // Add on click handlers for all the colored balls
        for(int i=0; i<colorOpts.length; i++){
            ImageView child = (ImageView) (layout.getChildAt(i));
            colorOpts[i] = child;
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String color = view.getTag().toString();
                    gameView.fillBall(color);
                }
            });
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runAI();
            }
        }, 500);
    }
    public void runAI(){
        HashMap<Character, String> map = new HashMap<Character, String>();
        map.put('B', "blue"); map.put('R', "red"); map.put('Y', "yellow");
        map.put('O', "orange"); map.put('G', "green"); map.put('P', "purple");

        // Have the AI play the game
        guess = getAIRes();
        System.out.println(guess);
        for(int i=0; i<guess.length(); i++){
            gameView.fillBall(map.get(guess.charAt(i)));
        }

        // Continue guessing
        if(running){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runAI();
                }
            }, 1000);
        }
    }
    public String getAIRes(){
        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject pyobj = py.getModule("Mastermind");
        PyObject obj = null;
        obj = pyobj.callAttr("run_game", prevGuesses);
        return obj.toString();
    }

    @Override
    public void onGameOver(boolean won) {
        running = false;
        if(won){
            gameOverText.setText("Code cracked in "+(7-curLayer)+" guesses!");
        }
        else{
            gameOverText.setText("Out of guesses!");
        }
        gameOverText.setVisibility(View.VISIBLE);
        resetButton.setVisibility(View.VISIBLE);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButton.setVisibility(View.GONE);
                gameOverText.setVisibility(View.GONE);
                recreate();
            }
        });
    }

    @Override
    public void onClueReceived(int[] info) {
        recentClue = info;
        prevGuesses += guess+"-"+recentClue[0]+"-"+recentClue[1]+";";
        if(recentClue[0]==4){
            onGameOver(true);
        }
        else if(curLayer==0){
            onGameOver(false);
        }
        curLayer--;
    }
}