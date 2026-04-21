package com.example.bookapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookapp.R;
import com.example.bookapp.adapter.BookAdapter;
import com.example.bookapp.database.AppDatabase;
import com.example.bookapp.model.BookModel;
import com.example.bookapp.network.BookApiService;
import com.example.bookapp.network.BookResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private RecyclerView recyclerView, recommendationRecyclerView;
    private BookAdapter adapter, recommendationAdapter;
    private List<BookModel> bookList = new ArrayList<>();
    private List<BookModel> recommendationList = new ArrayList<>();
    private ProgressBar progressBar;
    private SearchView searchView;
    private BottomNavigationView bottomNavigation;
    private TabLayout tabLayout;
    private TextView recommendationTitle;
    private BookApiService apiService;
    private AppDatabase db;
    private boolean isShowingFavorites = false;
    private String currentGenre = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        recommendationRecyclerView = findViewById(R.id.recommendationRecyclerView);
        recommendationTitle = findViewById(R.id.recommendationTitle);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        tabLayout = findViewById(R.id.tabLayout);

        db = AppDatabase.getInstance(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter(bookList, this);
        recyclerView.setAdapter(adapter);

        recommendationRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendationAdapter = new BookAdapter(recommendationList, this);
        recommendationRecyclerView.setAdapter(recommendationAdapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/books/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(BookApiService.class);

        setupTabs();
        setupSearchView();
        setupBottomNav();

        searchBooks("Java Programming", "All");
        loadRecommendations();
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentGenre = tab.getText().toString();
                if (!isShowingFavorites) {
                    searchBooks(currentGenre.equals("All") ? "Java Programming" : currentGenre, currentGenre);
                } else {
                    filterFavorites(searchView.getQuery().toString());
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!isShowingFavorites) {
                    searchBooks(query, currentGenre);
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (isShowingFavorites) {
                    filterFavorites(newText);
                }
                return false;
            }
        });
    }

    private void setupBottomNav() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                isShowingFavorites = false;
                tabLayout.setVisibility(View.VISIBLE);
                searchView.setQueryHint("Search books online...");
                searchBooks(currentGenre.equals("All") ? "Java" : currentGenre, currentGenre);
                return true;
            } else if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
                return false;
            } else if (itemId == R.id.nav_favorites) {
                isShowingFavorites = true;
                tabLayout.setVisibility(View.GONE);
                searchView.setQueryHint("Search in favorites...");
                loadFavorites();
                return true;
            }
            return false;
        });
    }

    private void loadRecommendations() {
        SharedPreferences prefs = getSharedPreferences("BookPrefs", MODE_PRIVATE);
        String lastGenre = prefs.getString("last_genre", null);

        if (lastGenre != null) {
            recommendationTitle.setVisibility(View.VISIBLE);
            recommendationRecyclerView.setVisibility(View.VISIBLE);
            recommendationTitle.setText("Because you liked " + lastGenre);

            apiService.searchBooks(lastGenre).enqueue(new Callback<BookResponse>() {
                @Override
                public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getItems() != null) {
                        recommendationList.clear();
                        int count = 0;
                        for (BookResponse.Item item : response.body().getItems()) {
                            if (count >= 5) break;
                            recommendationList.add(parseBook(item, lastGenre));
                            count++;
                        }
                        recommendationAdapter.updateList(recommendationList);
                    }
                }
                @Override
                public void onFailure(Call<BookResponse> call, Throwable t) {}
            });
        }
    }

    private BookModel parseBook(BookResponse.Item item, String genre) {
        BookResponse.VolumeInfo info = item.getVolumeInfo();
        String title = info.getTitle();
        String author = (info.getAuthors() != null && !info.getAuthors().isEmpty()) ? info.getAuthors().get(0) : "Unknown Author";
        String desc = info.getDescription() != null ? info.getDescription() : "No description available.";
        String image = info.getImageLinks() != null ? info.getImageLinks().getThumbnail().replace("http://", "https://") : "";
        return new BookModel(title, author, desc, image, genre);
    }

    private void searchBooks(String query, String genre) {
        progressBar.setVisibility(View.VISIBLE);
        apiService.searchBooks(query).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getItems() != null) {
                    bookList.clear();
                    for (BookResponse.Item item : response.body().getItems()) {
                        bookList.add(parseBook(item, genre));
                    }
                    adapter.updateList(bookList);
                } else {
                    Toast.makeText(MainActivity.this, "No books found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFavorites() {
        List<BookModel> favorites = db.bookDao().getAllFavorites();
        bookList.clear();
        bookList.addAll(favorites);
        adapter.updateList(bookList);
    }

    private void filterFavorites(String query) {
        List<BookModel> favorites = db.bookDao().getAllFavorites();
        List<BookModel> filtered = new ArrayList<>();
        for (BookModel book : favorites) {
            boolean matchesQuery = book.getTitle().toLowerCase().contains(query.toLowerCase());
            boolean matchesGenre = currentGenre.equals("All") || (book.getGenre() != null && book.getGenre().equals(currentGenre));
            if (matchesQuery && matchesGenre) {
                filtered.add(book);
            }
        }
        adapter.updateList(filtered);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_theme) {
            toggleTheme();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleTheme() {
        int mode = AppCompatDelegate.getDefaultNightMode();
        AppCompatDelegate.setDefaultNightMode(mode == AppCompatDelegate.MODE_NIGHT_YES ? 
                AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
    }

    @Override
    public void onBookClick(BookModel book) {
        Intent intent = new Intent(this, BookDetailsActivity.class);
        intent.putExtra("book", book);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecommendations();
        if (isShowingFavorites) loadFavorites();
    }
}
