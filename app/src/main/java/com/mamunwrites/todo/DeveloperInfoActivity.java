package com.mamunwrites.todo;

import com.mamunwrites.todo.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.appbar.MaterialToolbar;
import android.widget.Toast;
import java.util.List;
import android.content.pm.ResolveInfo;

public class DeveloperInfoActivity extends AppCompatActivity {
    
    private static final String DEVELOPER_EMAIL = "support@mamunwrites.com";
    private static final String DEVELOPER_WEBSITE = "https://mamunwrites.com";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_info);
        
        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Setup email button
        MaterialButton emailButton = findViewById(R.id.emailButton);
        emailButton.setOnClickListener(v -> {
            try {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + DEVELOPER_EMAIL));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                
                // Check if there are apps that can handle this intent
                List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(emailIntent, 0);
                
                if (resolveInfos != null && !resolveInfos.isEmpty()) {
                    // There are email apps available
                    startActivity(Intent.createChooser(emailIntent, "Send email using..."));
                } else {
                    // Try with a more general intent
                    Intent generalIntent = new Intent(Intent.ACTION_SEND);
                    generalIntent.setType("message/rfc822");
                    generalIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{DEVELOPER_EMAIL});
                    generalIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                    
                    try {
                        startActivity(Intent.createChooser(generalIntent, "Send email using..."));
                    } catch (android.content.ActivityNotFoundException e) {
                        // No email client available at all
                        Toast.makeText(this, getString(R.string.no_email_app), Toast.LENGTH_SHORT).show();
                        
                        // Fallback - copy email to clipboard
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Developer Email", DEVELOPER_EMAIL);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, getString(R.string.email_copied), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error opening email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                
                // Fallback to clipboard
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Developer Email", DEVELOPER_EMAIL);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, getString(R.string.email_copied), Toast.LENGTH_SHORT).show();
            }
        });
        
        // Setup website button
        MaterialButton websiteButton = findViewById(R.id.websiteButton);
        websiteButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(DEVELOPER_WEBSITE));
            startActivity(intent);
        });
        
        // Setup feedback button
        MaterialButton feedbackButton = findViewById(R.id.feedbackButton);
        feedbackButton.setOnClickListener(v -> {
            try {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + DEVELOPER_EMAIL));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_text));
                
                // Check if there are apps that can handle this intent
                List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(emailIntent, 0);
                
                if (resolveInfos != null && !resolveInfos.isEmpty()) {
                    // There are email apps available
                    startActivity(Intent.createChooser(emailIntent, "Send feedback using..."));
                } else {
                    // Try with a more general intent
                    Intent generalIntent = new Intent(Intent.ACTION_SEND);
                    generalIntent.setType("message/rfc822");
                    generalIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{DEVELOPER_EMAIL});
                    generalIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
                    generalIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_text));
                    
                    try {
                        startActivity(Intent.createChooser(generalIntent, "Send feedback using..."));
                    } catch (android.content.ActivityNotFoundException e) {
                        // No email client available at all
                        Toast.makeText(this, getString(R.string.no_email_app), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error opening email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
} 

