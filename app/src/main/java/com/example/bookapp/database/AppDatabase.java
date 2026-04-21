package com.example.bookapp.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.bookapp.model.BookModel;

@Database(entities = {BookModel.class}, version = 2) // Increased version to 2
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract BookDao bookDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "book_database")
                    .fallbackToDestructiveMigration() // This will clear the old database to fix the schema crash
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
