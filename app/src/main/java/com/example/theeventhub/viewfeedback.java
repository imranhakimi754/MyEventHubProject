package com.example.theeventhub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class viewfeedback extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FeedbackAdapter adapter;
    private List<Feedback> feedbackList;
    private RequestQueue requestQueue;
    private int userId;
    private int eventID;
    private ImageView backButton;
    private TextView totalFeedbackTextView;
    private TextView eventTitleTextView;
    private TextView viewAttendanceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewfeedback);

        userId = getIntent().getIntExtra("user_id", 0);
        eventID = getIntent().getIntExtra("EventID", -1);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        totalFeedbackTextView = findViewById(R.id.totalStudents);
        eventTitleTextView = findViewById(R.id.eventTitle);
        viewAttendanceText = findViewById(R.id.viewAttendanceText);

        feedbackList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        backButton = findViewById(R.id.backButton);

        fetchFeedbackData();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(viewfeedback.this, eventpage.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finish();
            }
        });

        viewAttendanceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(viewfeedback.this, viewattendance.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("EventID", eventID);
                intent.putExtra("previous_activity", "viewfeedback");
                startActivity(intent);
            }
        });
    }

    private void fetchFeedbackData() {
        String serverIp = getString(R.string.server_ip);
        String url = serverIp + "/theeventhub/fetch_feedback.php?EventID=" + eventID;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String eventTitle = response.getString("EventTitle");
                            int totalFeedback = response.getInt("TotalFeedback");

                            eventTitleTextView.setText(eventTitle);
                            totalFeedbackTextView.setText("Total Feedback: " + totalFeedback);

                            JSONArray feedbacks = response.getJSONArray("Feedbacks");
                            for (int i = 0; i < feedbacks.length(); i++) {
                                JSONObject feedbackObject = feedbacks.getJSONObject(i);
                                String reviewerName = feedbackObject.getString("user_fullname");
                                String reviewDate = feedbackObject.getString("feedbackdate");
                                String reviewText = feedbackObject.getString("feedback");

                                feedbackList.add(new Feedback(reviewerName, reviewDate, reviewText));
                            }
                            adapter = new FeedbackAdapter(feedbackList);
                            recyclerView.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(viewfeedback.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", error.toString());
                        Toast.makeText(viewfeedback.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private class DeleteEventTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            int eventID = params[0];
            int userId = params[1];
            boolean success = false;

            try {
                String serverIp = getString(R.string.server_ip);
                String urlString = serverIp + "/theeventhub/deleteevent.php?EventID=" + eventID + "&UserID=" + userId;
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (jsonResponse.has("success")) {
                        success = true;
                    } else {
                        Log.e("DeleteEventTask", "Error: " + jsonResponse.getString("error"));
                    }
                } else {
                    Log.e("DeleteEventTask", "HTTP error code: " + responseCode);
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.e("DeleteEventTask", "Exception: " + e.getMessage());
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(viewfeedback.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(viewfeedback.this, eventpage.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(viewfeedback.this, "Failed to delete event", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
