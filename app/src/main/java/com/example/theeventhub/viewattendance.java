package com.example.theeventhub;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class viewattendance extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private List<Student> studentList;
    private TextView eventTitleTextView;
    private TextView totalStudentsTextView;
    private Button btnAll, btnPresent, btnAbsent;

    private int userId;
    private int eventID;
    private String previousActivity;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewattendance);

        userId = getIntent().getIntExtra("user_id", 0);
        eventID = getIntent().getIntExtra("EventID", -1);
        previousActivity = getIntent().getStringExtra("previous_activity");

        eventTitleTextView = findViewById(R.id.eventTitle);
        totalStudentsTextView = findViewById(R.id.totalStudents);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAll = findViewById(R.id.btn_all);
        btnPresent = findViewById(R.id.btn_present);
        btnAbsent = findViewById(R.id.btn_absent);

        studentList = new ArrayList<>();
        adapter = new StudentAdapter(this, studentList, true, eventID);  // Initialize adapter with showAttendance set to true and pass eventID
        recyclerView.setAdapter(adapter);

        backButton = findViewById(R.id.backButton);

        if (eventID != -1) {
            new FetchAttendanceDetailsTask().execute(eventID);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if ("viewfeedback".equals(previousActivity)) {
                    intent = new Intent(viewattendance.this, viewfeedback.class);
                } else {
                    intent = new Intent(viewattendance.this, eventpage.class);
                }
                intent.putExtra("user_id", userId);
                intent.putExtra("EventID", eventID);
                startActivity(intent);
                finish();
            }
        });

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.filterList("all");
            }
        });

        btnPresent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.filterList("present");
            }
        });

        btnAbsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.filterList("absent");
            }
        });
    }

    private class FetchAttendanceDetailsTask extends AsyncTask<Integer, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Integer... params) {
            int eventID = params[0];
            String serverIp = getString(R.string.server_ip);
            String urlString = serverIp + "/theeventhub/attendance.php?eventID=" + eventID;

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
                Log.e("FetchAttendanceDetailsTask", "Error fetching attendance details", e);
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
                        student.setUserID(studentObject.getInt("userID")); // Set userID
                        student.setStatus(studentObject.getString("status"));
                        students.add(student);
                    }

                    studentList.clear();
                    studentList.addAll(students);
                    adapter.setStudentList(studentList); // Set the student list in the adapter
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Log.e("FetchAttendanceDetailsTask", "Error parsing JSON", e);
                }
            } else {
                Toast.makeText(viewattendance.this, "Error fetching attendance details", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
