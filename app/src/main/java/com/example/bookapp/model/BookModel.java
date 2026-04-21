package com.example.bookapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "favorites")
public class BookModel implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String author;
    private String description;
    private String imageUrl;
    private String genre; // New Field

    public BookModel(String title, String author, String description, String imageUrl, String genre) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.genre = genre;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
}
