package com.wangjessica.jwlab11b;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;

public class TwoplayerActivity extends AppCompatActivity implements MastermindView.ClueReceivedHandler{

    // Layout components
    MastermindView gameView;
    TextView waitingText;
    ImageView[] colorOpts = new ImageView[6];
    int[] myClue;
    int myLayer = 7;
    int id;
    int volleyCnt = 0;

    MastermindResultsView gameView2;
    int oppId;
    int numBlack;
    int numWhite;
    int layerId = 8;
    int prevLayer = 8;
    boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twoplayer);

        // Layout components
        waitingText = findViewById(R.id.waiting_text);
        gameView = findViewById(R.id.game_view);
        LinearLayout layout = findViewById(R.id.choices);
        gameView2 = findViewById(R.id.game_view2);

        for(int i=0; i<colorOpts.length; i++) {
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

        // Add the user to a room
        getUid("https://user.tjhsst.edu/2023jwang/add_room_getuid", "nextId");
    }
    public void continuouslyRequest(){
        waitingText.setVisibility(View.GONE);
        getOppInfo();
    }
    public void getUid(String url, String key){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        System.out.println("HELLO TRYING");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("GOT A RESPONSE");
                try{
                    id = Integer.parseInt(response.getString(key));
                    System.out.println("id: "+id);
                    getOppId("https://user.tjhsst.edu/2023jwang/get_opp_id");
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
    public void getOppId(String url){
        System.out.println("STARTING REQ");
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String urlWithData = url;
        urlWithData+="?";
        urlWithData+="id="+id;
        System.out.println(urlWithData);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, urlWithData, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("RESPONSE");
                try {
                    oppId = Integer.parseInt(response.getString("id1"));
                    System.out.println("oppId: "+oppId);
                    if(oppId==-1)
                        getOppId("https://user.tjhsst.edu/2023jwang/get_opp_id");
                    else
                        continuouslyRequest();
                } catch (JSONException e) {
                    oppId = -1;
                }
            }
        }, new Response.ErrorListener(){
            public void onErrorResponse(VolleyError error){
                error.printStackTrace();
            }
        });

        requestQueue.add(req);
    }
    public void getOppInfo(){
        System.out.println("GETTING OPP INFO");
        String url = "https://user.tjhsst.edu/2023jwang/opp_stats?oppid="+oppId;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("RESPONSE RECEIVED");
                volleyCnt--;
                try{
                    numBlack = Integer.parseInt(response.getString("numBlack"));
                    numWhite = Integer.parseInt(response.getString("numWhite"));
                    layerId = Integer.parseInt(response.getString("layer"));
                    System.out.println("AAAA LAYER ID: "+layerId);
                    if(layerId<prevLayer)
                        gameView2.giveClue(numBlack, numWhite);
                    prevLayer = layerId;
                    if(numBlack==4) {
                        waitingText.setVisibility(View.VISIBLE);
                        waitingText.setText("You lost");
                        running = false;
                        System.out.println("YOU LOST");
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonObjectRequest);
        volleyCnt++;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("count: "+volleyCnt);
                if(volleyCnt<10 && running)
                    getOppInfo();
            }
        }, 100);
    }
    public void sendGameInfo(){
        String url = "https://user.tjhsst.edu/2023jwang/update_stats_mastermind";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String urlWithData = url+"?";
        urlWithData += "numBlack="+myClue[0];
        urlWithData += "&numWhite="+myClue[1];
        urlWithData += "&layer="+myLayer;
        urlWithData += "&id="+id;

        System.out.println(urlWithData);

        if(myClue[0]==4){
            waitingText.setVisibility(View.VISIBLE);
            waitingText.setText("You won!");
            System.out.println("I WON");
            running = false;
        }
        myLayer--;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, urlWithData, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener(){
            public void onErrorResponse(VolleyError error){
                error.printStackTrace();
            }
        });

        requestQueue.add(req);
    }

    @Override
    public void onClueReceived(int[] info) {
        myClue = info;
        System.out.println("CLUEEEE RECEIVED");
        for(int i: myClue) System.out.print(i+" ");
        System.out.println();
        sendGameInfo();
    }
}