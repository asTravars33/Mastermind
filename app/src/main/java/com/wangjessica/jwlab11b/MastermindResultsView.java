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

public class MastermindResultsView extends View{

    // Dimensions
    int width;
    int height;

    // Circles
    Circle[][] clueCircles = new Circle[8][4];

    // Game variables
    int curLayer = 7;

    // Paint
    Paint p;

    public MastermindResultsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        width = getWidth();
        height = getHeight();

        int layerHeight = height/8;
        int clueRadius = layerHeight/4;

        // Instantiate all of the circles
        int col = getResources().getColor(R.color.def, null);
        for(int i=0; i<8; i++){
            for(int j=0; j<4; j++){
                // clue circle
                int y = layerHeight*i+clueRadius;
                int x = clueRadius;
                if(j>=2)
                    y+=(2*clueRadius);
                if(j==1 || j==3)
                    x+=(2*clueRadius);
                clueCircles[i][j] = new Circle(x, y, clueRadius-5, col);
            }
        }

        // Initialize the Paint
        p = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(Circle[] row: clueCircles){
            for(Circle c: row){
                p.setColor(c.color);
                canvas.drawCircle(c.x, c.y, c.radius, p);
            }
        }
    }

    public void giveClue(int numBlack, int numWhite){
        System.out.println("GIVING CLUE: "+numBlack+" "+numWhite);
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
        curLayer--;
        invalidate();
    }
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
}