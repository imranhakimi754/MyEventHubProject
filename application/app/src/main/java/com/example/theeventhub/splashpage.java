package com.example.theeventhub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class splashpage extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 1000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashpage);

        // Initialize views
        ImageView imageView = findViewById(R.id.imageView);
        TextView welcomeTextView = findViewById(R.id.welcome);
        ProgressBar progressBar = findViewById(R.id.progressbar);

        // Set the drawable for the imageView (assuming you have a drawable named logo_)
        imageView.setImageResource(R.drawable.logo_);

        // Set the text and style for the welcomeTextView
        welcomeTextView.setText("Welcome to EventHub");
        welcomeTextView.setTextSize(20);
        welcomeTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);

        // Set the progress bar color (already set in XML, so no need to do it here)
        progressBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(android.R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

        // Delay for splash screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(splashpage.this, loginpage.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
