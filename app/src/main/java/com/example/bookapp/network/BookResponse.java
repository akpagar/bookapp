package com.example.bookapp.network;

import java.util.List;

public class BookResponse {
    private List<Item> items;

    public List<Item> getItems() { return items; }

    public static class Item {
        private VolumeInfo volumeInfo;
        public VolumeInfo getVolumeInfo() { return volumeInfo; }
    }

    public static class VolumeInfo {
        private String title;
        private List<String> authors;
        private String description;
        private ImageLinks imageLinks;

        public String getTitle() { return title; }
        public List<String> getAuthors() { return authors; }
        public String getDescription() { return description; }
        public ImageLinks getImageLinks() { return imageLinks; }
    }

    public static class ImageLinks {
        private String thumbnail;
        public String getThumbnail() { return thumbnail; }
    }
}
