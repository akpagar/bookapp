package com.example.bookapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.bookapp.model.BookModel;
import java.util.List;

@Dao
public interface BookDao {
    @Insert
    void insertFavorite(BookModel book);

    @Delete
    void deleteFavorite(BookModel book);

    @Query("SELECT * FROM favorites")
    List<BookModel> getAllFavorites();

    @Query("SELECT * FROM favorites WHERE title = :title LIMIT 1")
    BookModel getFavoriteByTitle(String title);
}
