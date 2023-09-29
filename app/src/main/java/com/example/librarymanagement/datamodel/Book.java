package com.example.librarymanagement.datamodel;

public class Book {
    private String author;
    private boolean available;
    private String bookName;
    private String category;
    private String image;

    public Book() {
    }

    public Book(String author, boolean available, String bookName, String category, String image) {
        this.author = author;
        this.available = available;
        this.bookName = bookName;
        this.category = category;
        this.image = image;
    }

    public Book(String bookName, String author, String image) {
        this.bookName = bookName;
        this.author = author;
        this.image = image;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
