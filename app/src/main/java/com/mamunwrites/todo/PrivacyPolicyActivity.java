package com.mamunwrites.todo;

import com.mamunwrites.todo.R;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class PrivacyPolicyActivity extends AppCompatActivity {
    
    private static final String CONTACT_EMAIL = "support@mamunwrites.com";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        
        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Setup contact button
        MaterialButton contactButton = findViewById(R.id.contactButton);
        contactButton.setOnClickListener(v -> {
            try {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + CONTACT_EMAIL));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.privacy_policy) + " - " + getString(R.string.app_name));
                
                // Check if there are apps that can handle this intent
                List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(emailIntent, 0);
                
                if (resolveInfos != null && !resolveInfos.isEmpty()) {
                    // There are email apps available
                    startActivity(Intent.createChooser(emailIntent, getString(R.string.email_chooser_title)));
                } else {
                    // Try with a more general intent
                    Intent generalIntent = new Intent(Intent.ACTION_SEND);
                    generalIntent.setType("message/rfc822");
                    generalIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{CONTACT_EMAIL});
                    generalIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.privacy_policy) + " - " + getString(R.string.app_name));
                    
                    try {
                        startActivity(Intent.createChooser(generalIntent, getString(R.string.email_chooser_title)));
                    } catch (android.content.ActivityNotFoundException e) {
                        // No email client available at all
                        Toast.makeText(this, getString(R.string.no_email_app), Toast.LENGTH_SHORT).show();
                        
                        // Fallback - copy email to clipboard
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Contact Email", CONTACT_EMAIL);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, getString(R.string.email_copied), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error opening email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                
                // Fallback to clipboard
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Contact Email", CONTACT_EMAIL);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, getString(R.string.email_copied), Toast.LENGTH_SHORT).show();
            }
        });
    }
} 

