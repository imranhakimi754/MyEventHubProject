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

public class verificationpage extends AppCompatActivity {

    Button verify;
    EditText editTextVerificationCode;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificationpage);

        verify = findViewById(R.id.buttonVerify);
        editTextVerificationCode = findViewById(R.id.editTextVerificationCode);

        // Get email from intent
        Intent intent = getIntent();
        if (intent != null) {
            email = intent.getStringExtra("email");
        }

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verificationCode = editTextVerificationCode.getText().toString().trim();
                if (!verificationCode.isEmpty()) {
                    // Send verification code and email to server for validation
                    new VerifyVerificationCodeTask().execute(email, verificationCode);
                } else {
                    Toast.makeText(verificationpage.this, "Please enter verification code", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class VerifyVerificationCodeTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String verificationCode = params[1];
            String serverUrl = getString(R.string.server_ip) + "/theeventhub/verify_verification_code.php"; // Update with your server address

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                // Build POST data
                Map<String, String> postData = new HashMap<>();
                postData.put("email", email);
                postData.put("verification_code", verificationCode);

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
                    // Verification successful, proceed to newpassword activity
                    Intent intent = new Intent(verificationpage.this, newpassword.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish(); // Close current activity
                } else {
                    String message = jsonObject.getString("message");
                    Toast.makeText(verificationpage.this, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(verificationpage.this, "Error parsing response", Toast.LENGTH_SHORT).show();
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
