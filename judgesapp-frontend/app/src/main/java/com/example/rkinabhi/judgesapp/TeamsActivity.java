package com.example.rkinabhi.judgesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeamsActivity extends AppCompatActivity {
    private static final String TAG = "TeamsActivity";
    private static final String base = "https://ingenius-judges.herokuapp.com";

    ScrollView sv;
    LinearLayout ll;
    Spinner judgesSP;

    ArrayList<Team> teams;
    Map<String, Integer> judges;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams);

        sv = findViewById(R.id.scroll);
        ll = findViewById(R.id.container);
        judgesSP = findViewById(R.id.judge_spinner);
        getTeams();

    }

    @Override
    public void onBackPressed() {

    }

    void filterTeamsOnJudge(final int judgeID){
       for(Team t : teams){
           ll.removeView(t.view);
       }
        if(judgeID == 0){
            for (Team t : teams) {
                t.addViewToLayout();
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                List<Team> filteredList = teams.stream().filter(t -> t.primary_judge_id == judgeID || t.secondary_judge_id == judgeID).collect(Collectors.toList());
                Log.d(TAG, "filterTeamsOnJudge: " + Integer.toString(filteredList.size()));
                for (Team t : filteredList) {
                    t.addViewToLayout();
                }
            }
        }
    }

    void getTeams(){
        String url = base+"/getTeams";
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        teams = new ArrayList<Team>();
                        judges = new LinkedHashMap<>();
                        judges.put("ALL TEAMS", 0);
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONArray jsonArray = response.getJSONArray(i);
                                boolean isPrimaryJudged;
                                boolean isSecondaryJudged;
                                if(jsonArray.getString(7).equals("NO"))
                                    isPrimaryJudged = false;
                                else
                                    isPrimaryJudged = true;
                                if(jsonArray.getString(8).equals("NO"))
                                    isSecondaryJudged = false;
                                else
                                    isSecondaryJudged = true;

                                teams.add(
                                        new Team(
                                                jsonArray.getString(0),
                                                jsonArray.getInt(1),
                                                jsonArray.getInt(2),
                                                jsonArray.getString(3),
                                                jsonArray.getInt(4),
                                                jsonArray.getString(5),
                                                jsonArray.getInt(6),
                                                isPrimaryJudged,
                                                isSecondaryJudged
                                        )
                                );
                                judges.put(jsonArray.getString(3), jsonArray.getInt(4));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        String[] judgesArray = new String[judges.size()];
                        judges.keySet().toArray(judgesArray);
                        ArrayAdapter judgesArrayAdapter = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_spinner_item, judgesArray);
                        judgesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        judgesSP.setAdapter(judgesArrayAdapter);

                        judgesSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    filterTeamsOnJudge(judges.get(parent.getItemAtPosition(position)));
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        Volley.newRequestQueue(getApplicationContext()).add(getRequest);
    }

    public class Team {
        View view;
        TextView teamNameTV;
        TextView primaryJudgeNameTV;
        TextView secondaryJudgeNameTV;
        TextView teamIdTV;
        TextView teamRoomTV;
        Button primaryJudgeBT;
        Button secondaryJudgeBT;
        public String team_name;
        public Integer team_id;
        public Integer team_room;
        public String primary_judge_name;
        public Integer primary_judge_id;
        public String secondary_judge_name;
        public Integer secondary_judge_id;

        public Team(final String team_name, final Integer team_id, final Integer team_room, final String primary_judge_name,
                    final Integer primary_judge_id, final String secondary_judge_name, final Integer secondary_judge_id, boolean isPrimaryJudged, boolean isSecondaryJudged) {
            view = getLayoutInflater().inflate(R.layout.morning_teams_list_item, ll, false);
            teamNameTV = view.findViewById(R.id.team_name);
            primaryJudgeNameTV = view.findViewById(R.id.primary_judge_name);
            secondaryJudgeNameTV = view.findViewById(R.id.secondary_judge_name);
            teamIdTV = view.findViewById(R.id.team_id);
            teamRoomTV = view.findViewById(R.id.team_room);
            primaryJudgeBT = view.findViewById(R.id.primary_button);
            secondaryJudgeBT = view.findViewById(R.id.secondary_button);
            this.team_name = team_name;
            this.team_id = team_id;
            this.team_room = team_room;
            this.primary_judge_name = primary_judge_name;
            this.primary_judge_id = primary_judge_id;
            this.secondary_judge_name = secondary_judge_name;
            this.secondary_judge_id = secondary_judge_id;
            teamNameTV.setText(team_name);
            teamIdTV.setText("TEAM ID : " + team_id.toString());
            teamRoomTV.setText("ROOM # : " + team_room.toString());
            primaryJudgeNameTV.setText(primary_judge_name);
            secondaryJudgeNameTV.setText(secondary_judge_name);
            if(isPrimaryJudged){
                primaryJudgeBT.setBackgroundColor(getResources().getColor(R.color.judged));
                primaryJudgeBT.setEnabled(false);
            } else {
                primaryJudgeBT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), JudgingActivity.class);
                        intent.putExtra("team_name", team_name);
                        intent.putExtra("team_id", team_id);
                        intent.putExtra("judge_name", primary_judge_name);
                        intent.putExtra("judge_id", primary_judge_id);
                        startActivity(intent);
                        finish();
                    }
                });
            }
            if(isSecondaryJudged){
                secondaryJudgeBT.setBackgroundColor(getResources().getColor(R.color.judged));
                secondaryJudgeBT.setEnabled(false);
            } else {
                secondaryJudgeBT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), JudgingActivity.class);
                        intent.putExtra("team_name", team_name);
                        intent.putExtra("team_id", team_id);
                        intent.putExtra("judge_name", secondary_judge_name);
                        intent.putExtra("judge_id", secondary_judge_id);
                        startActivity(intent);
                        finish();
                    }
                });
            }
            addViewToLayout();
        }

        void addViewToLayout(){
            ll.addView(view);
        }
    }
}
