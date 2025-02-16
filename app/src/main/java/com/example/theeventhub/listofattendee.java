package com.example.theeventhub;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class listofattendee extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private List<Student> studentList;
    private TextView eventTitleTextView;
    private TextView totalStudentsTextView;

    private int userId;
    private int eventID;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listofattendee);

        userId = getIntent().getIntExtra("user_id", 0);
        eventID = getIntent().getIntExtra("EventID", -1);

        eventTitleTextView = findViewById(R.id.eventTitle);
        totalStudentsTextView = findViewById(R.id.totalStudents);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        studentList = new ArrayList<>();
        adapter = new StudentAdapter(this, studentList, false, eventID);  // Initialize adapter with showAttendance set to false and pass eventID
        recyclerView.setAdapter(adapter);

        backButton = findViewById(R.id.backButton);

        if (eventID != -1) {
            new FetchEventDetailsTask().execute(eventID);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(listofattendee.this, manageevent.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("EventID", eventID);
                startActivity(intent);
                finish();
            }
        });
    }

    private class FetchEventDetailsTask extends AsyncTask<Integer, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Integer... params) {
            int eventID = params[0];
            String serverIp = getString(R.string.server_ip);
            String urlString = serverIp + "/theeventhub/registeredstudentthisevent.php?eventID=" + eventID;

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                reader.close();
                connection.disconnect();

                return new JSONObject(stringBuilder.toString());

            } catch (Exception e) {
                Log.e("FetchEventDetailsTask", "Error fetching event details", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                try {
                    String eventTitle = result.getString("EventTitle");
                    int totalStudents = result.getInt("total_students");

                    eventTitleTextView.setText(eventTitle);
                    totalStudentsTextView.setText("Total Participants: " + totalStudents);

                    JSONArray studentsArray = result.getJSONArray("students");
                    List<Student> students = new ArrayList<>();

                    for (int i = 0; i < studentsArray.length(); i++) {
                        JSONObject studentObject = studentsArray.getJSONObject(i);
                        Student student = new Student();
                        student.setUserFullname(studentObject.getString("user_fullname"));
                        student.setStudentId(studentObject.getString("studentid"));
                        student.setUserID(studentObject.getInt("userID")); // Make sure to set userID
                        students.add(student);
                    }

                    studentList.clear();
                    studentList.addAll(students);
                    adapter.setStudentList(studentList); // Set the student list in the adapter
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Log.e("FetchEventDetailsTask", "Error parsing JSON", e);
                }
            } else {
                Toast.makeText(listofattendee.this, "Error fetching event details", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
