package com.example.testnotes1;

import java.util.ArrayList;
import java.util.Date;

public class Note {
    //private final Date date;
    private String title;
    private String content;
    private static int nextId = 0;
    private long id;
    private String timeStamp;
    private ArrayList<String> tags;
    private boolean isPinned;

    public Note(String title, String content, String timeStamp, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.timeStamp = timeStamp;
        this.isPinned = isPinned;
        //this.date = date;
    }


    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timeStamp;
    }

    public long getId(){
        return id;
    }

    public boolean getPinned(){
        return isPinned;
    }

    public void setId(long id){
        this.id = id;
    }

    public void setPinned(boolean isPinned){
        this.isPinned = isPinned;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
