package com.example.common;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
    private String id;
    private String title;
    private String description;
    private boolean completed;
    private Date dueDate;
    private boolean isSpecial;

    public Task(String id, String title, String description, Date dueDate, boolean isSpecial) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = false;
        this.isSpecial = isSpecial;
    }
    public Task() {
    }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public Date getDueDate() { return dueDate; }
    public boolean isSpecial() {
        return isSpecial;
    }
    public void setSpecial(boolean special) {
        isSpecial = special;
    }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
