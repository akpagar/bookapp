package com.example.bookapp.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bookapp.R;
import com.example.bookapp.database.AppDatabase;
import com.example.bookapp.model.BookModel;

public class BookDetailsActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView titleView, authorView, descriptionView;
    private Button btnFavorite;
    private BookModel book;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        imageView = findViewById(R.id.detailImage);
        titleView = findViewById(R.id.detailTitle);
        authorView = findViewById(R.id.detailAuthor);
        descriptionView = findViewById(R.id.detailDescription);
        btnFavorite = findViewById(R.id.btnFavorite);

        db = AppDatabase.getInstance(this);

        book = (BookModel) getIntent().getSerializableExtra("book");

        if (book != null) {
            titleView.setText(book.getTitle());
            authorView.setText(book.getAuthor());
            descriptionView.setText(book.getDescription());
            Glide.with(this).load(book.getImageUrl()).into(imageView);

            checkIfFavorite();
            trackView(); // New: Track viewing
        }

        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void trackView() {
        // Track total books viewed
        SharedPreferences prefs = getSharedPreferences("BookPrefs", MODE_PRIVATE);
        int totalViewed = prefs.getInt("total_viewed", 0);
        prefs.edit().putInt("total_viewed", totalViewed + 1).apply();

        // Track last viewed genre for recommendations
        if (book.getGenre() != null) {
            prefs.edit().putString("last_genre", book.getGenre()).apply();
        }
    }

    private void checkIfFavorite() {
        BookModel favorite = db.bookDao().getFavoriteByTitle(book.getTitle());
        if (favorite != null) {
            btnFavorite.setText("Remove from Favorites");
        } else {
            btnFavorite.setText("Save to Favorites");
        }
    }

    private void toggleFavorite() {
        BookModel favorite = db.bookDao().getFavoriteByTitle(book.getTitle());
        if (favorite != null) {
            db.bookDao().deleteFavorite(favorite);
            btnFavorite.setText("Save to Favorites");
            Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
        } else {
            db.bookDao().insertFavorite(book);
            btnFavorite.setText("Remove from Favorites");
            Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
        }
    }
}
