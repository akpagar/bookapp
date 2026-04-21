package com.example.bookapp.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookapp.R;
import com.example.bookapp.database.AppDatabase;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvTotalViewed, tvTotalFavorites;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvTotalViewed = findViewById(R.id.tvTotalViewed);
        tvTotalFavorites = findViewById(R.id.tvTotalFavorites);
        db = AppDatabase.getInstance(this);

        // Load stats
        loadStats();
    }

    private void loadStats() {
        // 1. Total Viewed from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("BookPrefs", MODE_PRIVATE);
        int totalViewed = prefs.getInt("total_viewed", 0);
        tvTotalViewed.setText(String.valueOf(totalViewed));

        // 2. Total Favorites from Database
        int totalFavorites = db.bookDao().getAllFavorites().size();
        tvTotalFavorites.setText(String.valueOf(totalFavorites));
    }
}
