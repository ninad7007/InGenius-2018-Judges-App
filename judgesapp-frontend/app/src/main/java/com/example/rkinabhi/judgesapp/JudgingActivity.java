package com.example.rkinabhi.judgesapp;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.xw.repo.BubbleSeekBar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JudgingActivity extends AppCompatActivity {
    private static final String TAG = "JudgingActivity";
    List<Person> judges;
    List<Person> teams;
    String base = "https://ingenius-judges.herokuapp.com";
    String criteria[] = {"Technical_Difficulty", "Completeness", "Originality", "Feasibility"};
    HashMap<String, ScoreBar> scoreBars;
    LinearLayout criteriaLL;
    TextView judgeNameTV;
    TextView teamNameTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        judgeNameTV = findViewById(R.id.judge_name);
        judgeNameTV.setText("JUDGE : "+getIntent().getStringExtra("judge_name"));
        teamNameTV = findViewById(R.id.team_name);
        teamNameTV.setText("TEAM : "+getIntent().getStringExtra("team_name"));

        criteriaLL = findViewById(R.id.criteraLL);
        loadData();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), TeamsActivity.class);
        startActivity(intent);
        finish();
    }

    void loadData(){

        scoreBars = new HashMap<>();
        for(String crit : criteria){
            scoreBars.put(
                    crit,
                    new ScoreBar(crit)
            );
        }

        Button submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                findViewById(R.id.submit).setVisibility(View.INVISIBLE);
                int judgeId = getIntent().getIntExtra("judge_id", 0);
                int teamId = getIntent().getIntExtra("team_id", 0);
                HashMap<String, Integer> scores = new HashMap<>();
                for(ScoreBar s : scoreBars.values()){
                    scores.put(s.criteriaNameDisplay.getText().toString(), s.score);
                }
                submitScore(judgeId, teamId, scores);
            }
        });
    }

    void submitScore(final int judgeId, final int teamId, final HashMap<String, Integer> scores){

        String url = base+"/insertScore";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                        findViewById(R.id.submit).setVisibility(View.VISIBLE);
                        switch(response){
                            case "SCORE_INSERTED": {
                                for(ScoreBar scoreBar : scoreBars.values()){
                                    scoreBar.seekBar.setProgress(0);
                                }
                                Toast.makeText(JudgingActivity.this, "SCORE HAS BEEN INSERTED", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), TeamsActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                            }
                            case "ALREADY_JUDGED":{
                                Toast.makeText(JudgingActivity.this, "THIS TEAM HAS ALREADY BEEN JUDGED", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            default: break;
                        }
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                        findViewById(R.id.submit).setVisibility(View.VISIBLE);
                        Toast.makeText(JudgingActivity.this, "SOME ERROR OCCURED", Toast.LENGTH_SHORT).show();
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("judgeID", Integer.toString(judgeId));
                params.put("teamID", Integer.toString(teamId));
                for(String criteria : scores.keySet())
                    params.put(criteria, Integer.toString(scores.get(criteria)));
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
    }

    public class Person {
        public int id;
        public String name;

        Person(int id, String name){
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public class ScoreBar{
        View view;
        BubbleSeekBar seekBar;
        TextView criteriaNameDisplay;
        TextView criteriaScoreDisplay;
        int score = 0;

        ScoreBar(String criteriaName){
            view = getLayoutInflater().inflate(R.layout.criteria_element, criteriaLL, false);
            criteriaNameDisplay = view.findViewById(R.id.criteria_name_display);
            seekBar = view.findViewById(R.id.criteria_score);
            criteriaScoreDisplay = view.findViewById(R.id.criteria_score_display);

            criteriaNameDisplay.setText(criteriaName);

            seekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
                @Override
                public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                    score = progress;
                    criteriaScoreDisplay.setText(String.valueOf(score));
                }

                @Override
                public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

                }

                @Override
                public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

                }
            });

            criteriaLL.addView(view);
        }
    }

}
