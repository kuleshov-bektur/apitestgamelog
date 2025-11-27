package com.example.gamecatalog;

public class Game {
    private String title;
    private String platform;
    private int year;
    private String genre;
    private String cover;
    private String developer;
    private String description;

    public Game() {}

    // геттеры
    public String getTitle() { return title; }
    public String getPlatform() { return platform; }
    public int getYear() { return year; }
    public String getGenre() { return genre; }
    public String getCover() { return cover; }
    public String getDeveloper() { return developer; }
    public String getDescription() { return description; }
}