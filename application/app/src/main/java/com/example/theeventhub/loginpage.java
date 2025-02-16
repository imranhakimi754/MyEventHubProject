package com.example.theeventhub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class loginpage extends AppCompatActivity {

    private EditText studentIDEditText, passwordEditText;
    private Button loginButton;
    private TextView textViewSignUp, forgotpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);

        // Initialize views
        studentIDEditText = findViewById(R.id.editTextStudentID);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        textViewSignUp = findViewById(R.id.textViewSignUp);
        forgotpassword = findViewById(R.id.textViewForgotPassword);

        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(loginpage.this, forgotpassword.class));
            }
        });

        // Set click listener for sign up text
        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to sign up activity
                startActivity(new Intent(loginpage.this, signuppage.class));
            }
        });

        // Set click listener for login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve username and password from EditText fields
                String studentid = studentIDEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(studentid) || TextUtils.isEmpty(password)) {
                    Toast.makeText(loginpage.this, "Please enter both Username and Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Perform login operation
                loginUser(studentid, password);
            }
        });
    }

    private void loginUser(final String studentid, final String password) {
        // Execute AsyncTask to perform network operation
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    // Construct the request URL
                    String serverIp = getString(R.string.server_ip);
                    String urlString = serverIp + "/theeventhub/loginpage.php"; // Replace with your login endpoint

                    // Construct the query parameters
                    String parameters = "studentid=" + URLEncoder.encode(studentid, "UTF-8") +
                            "&password=" + URLEncoder.encode(password, "UTF-8");

                    // Open connection
                    URL url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);

                    // Write data to the connection
                    OutputStream outputStream = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.write(parameters);
                    writer.flush();
                    writer.close();
                    outputStream.close();

                    // Get the response
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    inputStream.close();

                    // Return the response
                    return response.toString();

                } catch (IOException e) {
                    e.printStackTrace();
                    // Return an error message
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                // Handle the response
                if (result.startsWith("Success")) {
                    // Extract user_id from the response (assuming it's in the format "Success: user_id")
                    String[] parts = result.split(":");
                    if (parts.length == 2) {
                        int userId = Integer.parseInt(parts[1].trim());
                        // Store user_id in session (SharedPreferences)
                        saveUserIdInSession(userId);
                        // Navigate to the next activity
                        navigateToNextActivity(userId);
                    } else {
                        // Invalid response format
                        showToast("Invalid response from server");
                    }
                } else {
                    // Login failed, display an error message
                    showToast(result);
                }
            }
        }.execute();
    }

    private void saveUserIdInSession(int userId) {
        SharedPreferences sharedPref = getSharedPreferences("MySession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("userId", userId); // Use putInt to store an integer value
        editor.apply();
    }

    private void navigateToNextActivity(int userId) {
        Intent intent = new Intent(loginpage.this, dashboardpage.class);
        intent.putExtra("user_id", userId); // Put the user_id in the intent
        startActivity(intent);
        finish(); // Finish the current activity to prevent going back to it using the back button
    }

    private void showToast(String message) {
        Toast.makeText(loginpage.this, message, Toast.LENGTH_SHORT).show();
    }

}
