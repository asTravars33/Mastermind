package com.wangjessica.jwlab11b;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView rules;
    Button helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rules = findViewById(R.id.instructions);
        helpButton = findViewById(R.id.help_button);

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRules(null);
            }
        });
    }

    public void playIndividual(View view) {
        Intent intent = new Intent(MainActivity.this, IndividualActivity.class);
        startActivity(intent);
    }

    public void playTwoPlayer(View view) {
        Intent intent = new Intent(MainActivity.this, TwoplayerActivity.class);
        startActivity(intent);
    }
    public void playAI(View view){
        Intent intent = new Intent(MainActivity.this, GeneticAlgActivity.class);
        startActivity(intent);
    }

    public void showRules(View view){
        rules.setVisibility(View.VISIBLE);
        helpButton.setText("Hide Rules");
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissRules(view);
            }
        });
    }
    public void dismissRules(View view){
        rules.setVisibility(View.GONE);
        helpButton.setText("Show Rules");
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRules(view);
            }
        });
    }
}