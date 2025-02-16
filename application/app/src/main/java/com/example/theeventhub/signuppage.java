package com.example.theeventhub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class signuppage extends AppCompatActivity {

    private EditText editTextFullName, editTextStudentID, editTextEmail, editTextPhoneNumber, editTextPassword;
    private Spinner editTextFaculty;
    private TextView textViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signuppage);

        // Initialize your UI components
        textViewLogin = findViewById(R.id.textViewLogin);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextStudentID = findViewById(R.id.editTextStudentID);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextFaculty = findViewById(R.id.editTextFaculty);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getResources().getStringArray(R.array.faculty_array)) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item
                return position != 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                // Set the text color to white
                tv.setTextColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                // Set the disabled item text color to grey and others to white
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editTextFaculty.setAdapter(adapter);

        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get values from EditText fields
                String fullName = editTextFullName.getText().toString().trim();
                String studentid = editTextStudentID.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String faculty = editTextFaculty.getSelectedItem().toString().trim();
                String phone_num = editTextPhoneNumber.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Check if all fields are filled
                if (fullName.isEmpty() || studentid.isEmpty() || email.isEmpty() || faculty.isEmpty() || phone_num.isEmpty() || password.isEmpty()) {
                    Toast.makeText(signuppage.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if email belongs to the allowed domains
                String allowedStudentDomain = "@student.uitm.edu.my";
                String allowedStaffDomain = "@uitm.edu.my";
                if (!email.endsWith(allowedStudentDomain) && !email.endsWith(allowedStaffDomain)) {
                    Toast.makeText(signuppage.this, "Please use a valid UITM email address.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if password is at least 8 characters long
                if (password.length() < 8) {
                    Toast.makeText(signuppage.this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create HashMap to store data
                HashMap<String, String> postDataParams = new HashMap<>();
                postDataParams.put("fullName", fullName);
                postDataParams.put("studentid", studentid);
                postDataParams.put("email", email);
                postDataParams.put("faculty", faculty);
                postDataParams.put("phone_num", phone_num);
                postDataParams.put("password", password);

                // Send data to server
                sendDataToServer(postDataParams);
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to login activity
                startActivity(new Intent(signuppage.this, loginpage.class));
            }
        });
    }

    private void sendDataToServer(final HashMap<String, String> postDataParams) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Set up connection
                    String serverIp = getString(R.string.server_ip);
                    URL url = new URL(serverIp + "/theeventhub/signup.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    // Write data to output stream
                    OutputStream os = conn.getOutputStream();
                    StringBuilder postData = new StringBuilder();
                    for (String key : postDataParams.keySet()) {
                        if (postData.length() != 0) postData.append('&');
                        postData.append(key).append('=').append(postDataParams.get(key));
                    }
                    os.write(postData.toString().getBytes());
                    os.flush();
                    os.close();

                    // Get response code and message
                    int responseCode = conn.getResponseCode();
                    InputStream is = conn.getInputStream();
                    StringBuilder response = new StringBuilder();
                    int chr;
                    while ((chr = is.read()) != -1) {
                        response.append((char) chr);
                    }
                    is.close();

                    final String serverResponse = response.toString();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (serverResponse.contains("Error: Please use a valid UITM student email address.") ||
                                        serverResponse.contains("Error: Please use a valid UITM email address.")) {
                                    Toast.makeText(signuppage.this, "Please use a valid UITM email address.", Toast.LENGTH_SHORT).show();
                                } else if (serverResponse.contains("Error: Password must be at least 8 characters long.")) {
                                    Toast.makeText(signuppage.this, "Password must be at least 8 characters long.", Toast.LENGTH_SHORT).show();
                                } else if (serverResponse.contains("Error: Email already exists. Please sign in.")) {
                                    Toast.makeText(signuppage.this, "Email already exists. Please sign in.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(signuppage.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(signuppage.this, loginpage.class);
                                    startActivity(intent);
                                    finish(); // Optional, if you want to finish the signuppage activity
                                }
                            }
                        });
                    } else {
                        // Error response from server
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(signuppage.this, "Error registering user. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(signuppage.this, "Error: Unable to connect to server. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
