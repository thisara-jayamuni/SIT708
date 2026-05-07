package com.example.lostandfound;

public class LostFoundItem implements java.io.Serializable {

    private final int    id;
    private final String title;
    private final String category;
    private final String location;
    private final String description;
    private final String timestamp;
    private final String name;
    private final String phone;
    private final String postType;
    private final byte[] imageBytes;

    public LostFoundItem(int id, String title, String category, String location,
                         String description, String timestamp,
                         String name, String phone, String postType, byte[] imageBytes) {
        this.id          = id;
        this.title       = title;
        this.category    = category;
        this.location    = location;
        this.description = description;
        this.timestamp   = timestamp;
        this.name        = name;
        this.phone       = phone;
        this.postType    = postType;
        this.imageBytes  = imageBytes;
    }

    public int    getId()          { return id; }
    public String getTitle()       { return title; }
    public String getCategory()    { return category; }
    public String getLocation()    { return location; }
    public String getDescription() { return description; }
    public String getTimestamp()   { return timestamp; }
    public String getName()        { return name; }
    public String getPhone()       { return phone; }
    public String getPostType()    { return postType; }
    public byte[] getImageBytes()  { return imageBytes; }
}