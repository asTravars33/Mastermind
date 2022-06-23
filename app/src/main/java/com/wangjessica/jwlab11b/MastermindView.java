package com.wangjessica.jwlab11b;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MastermindView extends View{

    // Dimensions
    int width;
    int height;

    // Circles
    Circle[][] guessCircles = new Circle[8][4];
    Circle[][] clueCircles = new Circle[8][4];

    // Game variables
    int curLayer = 7;
    int curCircle = 0;
    int[] code = new int[4];
    HashSet<Integer> codeCols = new HashSet<Integer>();

    // Game end components
    GameOverHandler handler;
    ClueHandler clueHandler;
    ClueReceivedHandler receivedHandler;

    // Paint
    Paint p;

    public MastermindView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        try{
            handler = (GameOverHandler) context;
        }
        catch(Exception e){}
        try{
            clueHandler = (ClueHandler) context;
        }
        catch(Exception e){}
        try{
            receivedHandler = (ClueReceivedHandler) context;
        }
        catch(Exception e){}
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        width = getWidth();
        height = getHeight();

        int layerHeight = height/8;
        int guessWidth = width*3/4;
        int guessRadius = guessWidth/8;
        int clueRadius = layerHeight/4;

        // Instantiate all of the circles
        int col = getResources().getColor(R.color.def, null);
        for(int i=0; i<8; i++) {
            for (int j = 0; j < 4; j++) {
                guessCircles[i][j] = new Circle(guessRadius * 2 * j + guessRadius, layerHeight * i + guessRadius, guessRadius-5, col);
            }
        }
        for(int i=0; i<8; i++){
            for(int j=0; j<4; j++){
                // clue circle
                int y = layerHeight*i+clueRadius;
                int x = guessRadius*8 + clueRadius;
                if(j>=2)
                    y+=(2*clueRadius);
                if(j==1 || j==3)
                    x+=(2*clueRadius);
                clueCircles[i][j] = new Circle(x, y, clueRadius-5, col);
            }
        }

        // Generate a random code
        int[] colChoices = {R.color.blue, R.color.green, R.color.orange, R.color.purple, R.color.red, R.color.yellow};
        String[] names = {"B", "G", "O", "P", "R", "Y"};
        String clueStr = "";
        for(int i=0; i<4; i++){
            int idx = (int)(Math.random()*6);
            code[i] = getResources().getColor(colChoices[idx]);
            codeCols.add(code[i]);
            clueStr+=names[idx];
        }
        System.out.println(clueStr);

        // Initialize the Paint
        p = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(Circle[] row: guessCircles){
            for(Circle c: row){
                p.setColor(c.color);
                canvas.drawCircle(c.x, c.y, c.radius, p);
            }
        }
        for(Circle[] row: clueCircles){
            for(Circle c: row){
                p.setColor(c.color);
                canvas.drawCircle(c.x, c.y, c.radius, p);
            }
        }
    }

    // Updating the game board
    public void fillBall(String color){
        // Update the current circle
        int colorId = getResources().getColor(getResources().getIdentifier(color, "color", "com.wangjessica.jwlab11b"), null);
        guessCircles[curLayer][curCircle].setColor(colorId);
        curCircle++;
        invalidate();
        // This layer has been filled?
        if(curCircle>=4){
            // Give em the clue
            giveClue();
            System.out.println("Giving the clue boi");
            // Update the layer
            curLayer--;
            curCircle=0;
            // Game over?
            if(curLayer<0){
                gameOver();
            }
        }
    }
    public void giveClue(){
        // Count the number of correct placements
        int correctCnt = 0;
        for(int i=0; i<4; i++){
            if(guessCircles[curLayer][i].getColor()==code[i])
                correctCnt++;
        }
        // Count the number of right color/wrong spot placements
        int[] clueInfo = clue();
        if(receivedHandler!=null)
            receivedHandler.onClueReceived(clueInfo);
        int numBlack = clueInfo[0];
        int numWhite = clueInfo[1];
        boolean won = (numBlack==4? true: false);
        // Update the display with the new information
        for(int i=0; i<clueCircles[curLayer].length; i++){
            if(numBlack>0){
                clueCircles[curLayer][i].setColor(Color.BLACK);
                numBlack--;
            }
            else if(numWhite>0){
                clueCircles[curLayer][i].setColor(Color.WHITE);
                numWhite--;
            }
        }
        // Has the user guessed the code?
        if(won && handler!=null){
            handler.onGameOver(true);
        }
    }
    public void gameOver(){
        // The user has not guessed the code.
        if(handler!=null)
            handler.onGameOver(false);
    }
    public int[] clue(){
        int numBlack = 0;
        int numWhite = 0;
        // Make a frequency map of the answer code
        HashMap<Integer, Integer> freqs = new HashMap<Integer, Integer>();
        for(int i: code)
            update(freqs, i, 1);
        // Go through the guess
        for(int i=0; i<guessCircles[curLayer].length; i++){
            int cur = guessCircles[curLayer][i].getColor();
            if(cur==code[i]){
                numBlack++;
                update(freqs, cur, -1);
            }
        }
        for(int i=0; i<guessCircles[curLayer].length; i++){
            int cur = guessCircles[curLayer][i].getColor();
            if(cur!=code[i] && freqs.containsKey(cur)) {
                numWhite++;
                update(freqs, cur, -1);
            }
        }
        // Return the clue
        int[] info = {numBlack, numWhite};
        return info;
    }
    public void update(HashMap<Integer, Integer> dict, int key, int val){
        if(!dict.containsKey(key))
            dict.put(key, val);
        else{
            dict.replace(key, dict.get(key)+val);
            if(dict.get(key)==0){
                dict.remove(key);
            }
        }
    }
    // Inner classes and interfaces
    private class Circle{
        int x;
        int y;
        int radius;
        int color;
        public Circle(int x, int y, int radius, int color){
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
        }
        public void setColor(int newCol){
            color = newCol;
        }
        public int getColor(){
            return color;
        }
    }
    public interface GameOverHandler{
        void onGameOver(boolean won);
    }
    public interface ClueHandler{
        void onNextClue();
    }
    public interface ClueReceivedHandler{
        void onClueReceived(int[] info);
    }
}