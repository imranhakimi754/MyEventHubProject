package com.example.theeventhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class forgotpassword extends AppCompatActivity {

    ImageButton backbutton;
    EditText editTextEmail;
    Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        backbutton = findViewById(R.id.buttonBack);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonSend = findViewById(R.id.buttonSend);

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(forgotpassword.this, loginpage.class));
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                if (!email.isEmpty()) {
                    // Call AsyncTask to send forgot password request
                    ForgotPasswordTask task = new ForgotPasswordTask();
                    task.execute(email);
                } else {
                    Toast.makeText(forgotpassword.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class ForgotPasswordTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String serverUrl = getString(R.string.server_ip) + "/theeventhub/forgot_password.php";

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Build POST data
                Map<String, String> postData = new HashMap<>();
                postData.put("email", email);

                // Write POST data to output stream
                OutputStream os = conn.getOutputStream();
                StringBuilder postDataString = new StringBuilder();
                for (Map.Entry<String, String> param : postData.entrySet()) {
                    if (postDataString.length() != 0) {
                        postDataString.append('&');
                    }
                    postDataString.append(param.getKey());
                    postDataString.append('=');
                    postDataString.append(param.getValue());
                }
                byte[] postDataBytes = postDataString.toString().getBytes("UTF-8");
                os.write(postDataBytes);
                os.flush();
                os.close();

                // Read response from server
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return response.toString();

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean success = jsonObject.getBoolean("success");

                    if (success) {
                        String email = editTextEmail.getText().toString().trim();
                        Intent intent = new Intent(forgotpassword.this, verificationpage.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish(); // Close current activity
                    } else {
                        String message = jsonObject.getString("message");
                        Toast.makeText(forgotpassword.this, message, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(forgotpassword.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(forgotpassword.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
