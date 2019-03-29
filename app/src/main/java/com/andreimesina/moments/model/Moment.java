package com.andreimesina.moments.model;

public class Moment {

    // Firestore online filename
    private String filename;
    private String imageUrl;
    private String story;
    private String location;

    public Moment() {

    }

    public Moment(String imageUrl, String story, String location) {
        if(story.trim().equals("")) {
            story = "No story";
        }

        if(location.trim().equals("")) {
            location = "No location";
        }

        this.imageUrl = imageUrl;
        this.story = story;
        this.location = location;
    }

    public Moment(String filename, String imageUrl, String story, String location) {
        if(story.trim().equals("")) {
            story = "No story";
        }

        if(location.trim().equals("")) {
            location = "No location";
        }

        this.filename = filename;
        this.imageUrl = imageUrl;
        this.story = story;
        this.location = location;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}