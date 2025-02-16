package com.example.theeventhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class newpassword extends AppCompatActivity {

    EditText etNewPassword, etConfirmPassword;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpassword);

        etNewPassword = findViewById(R.id.et_enter_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSubmit = findViewById(R.id.btn_submit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(newpassword.this, "Please enter both passwords", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPassword.length() < 8) {
                    Toast.makeText(newpassword.this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(newpassword.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Proceed to update password
                String email = getIntent().getStringExtra("email");
                new UpdatePasswordTask().execute(email, newPassword);
            }
        });
    }

    private class UpdatePasswordTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String newPassword = params[1];
            String serverUrl = getString(R.string.server_ip) + "/theeventhub/update_password.php"; // Update with your server address

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                // Build POST data
                Map<String, String> postData = new HashMap<>();
                postData.put("email", email);
                postData.put("new_password", newPassword);

                // Send POST data
                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(getPostDataString(postData).getBytes());
                outputStream.flush();
                outputStream.close();

                // Get response from server
                InputStream inputStream;
                if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                    inputStream = urlConnection.getInputStream();
                } else {
                    inputStream = urlConnection.getErrorStream();
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();

            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                boolean success = jsonObject.getBoolean("success");

                if (success) {
                    // Password updated successfully, proceed to login page
                    Toast.makeText(newpassword.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(newpassword.this, loginpage.class);
                    startActivity(intent);
                    finish(); // Close current activity
                } else {
                    String message = jsonObject.getString("message");
                    Toast.makeText(newpassword.this, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(newpassword.this, "Error parsing response", Toast.LENGTH_SHORT).show();
            }
        }

        private String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            return result.toString();
        }
    }
}
