package com.hardcodecoder.pulsemusic.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hardcodecoder.pulsemusic.BuildConfig;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.themes.ThemeManager;

public class AppInfo extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(ThemeManager.getThemeToApply());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        findViewById(R.id.app_dev_desc).setOnClickListener(v -> goToAuthor());
        findViewById(R.id.app_designer_desc).setOnClickListener(v -> goToAuthor());
        findViewById(R.id.app_testers_desc).setOnClickListener(v -> goToAuthor());

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toolbar t = findViewById(R.id.toolbar);
            t.setTitle(R.string.app_name);
            t.setNavigationIcon(R.drawable.ic_back);
            t.setNavigationOnClickListener(v -> finish());
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.app_info_close_btn).setOnClickListener(v -> finish());
        }

        TextView temp = findViewById(R.id.app_version_desc);
        temp.setText(BuildConfig.VERSION_NAME);

        temp = findViewById(R.id.app_build_desc);
        temp.setText(String.valueOf(BuildConfig.VERSION_CODE));

        findViewById(R.id.donate_btn).setOnClickListener(v -> buyMeCoffee());
    }

    private void goToAuthor() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.github_link)));
        startActivity(i);
    }

    private void buyMeCoffee() {
        Toast.makeText(this, "Thank you! No need to donate enjoy your pulse", Toast.LENGTH_LONG).show();
    }
}
