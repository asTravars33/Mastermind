package com.wangjessica.jwlab11b;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IndividualActivity extends AppCompatActivity implements MastermindView.GameOverHandler{

    // Layout components
    MastermindView gameView;
    ImageView[] colorOpts = new ImageView[6];

    // Game over components
    TextView gameOverText;
    Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mastermind);

        // Instantiate layout components
        gameView = findViewById(R.id.game_view);
        gameOverText = findViewById(R.id.game_over_msg);
        resetButton = findViewById(R.id.restart);
        LinearLayout layout = findViewById(R.id.choices);

        // Add on click handlers for all the colored circles
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
    }

    @Override
    public void onGameOver(boolean won) {
        System.out.println("I WAS CALLED");
        if(won){
            gameOverText.setText("You cracked the code!");
        }
        else{
            gameOverText.setText("Out of moves!");
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
}