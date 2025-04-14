package com.example.common;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
    private String id;
    private String title;
    private String description;
    private boolean completed;
    private Date dueDate;

    public Task(String id, String title, String description, Date dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public Date getDueDate() { return dueDate; }

    public void setCompleted(boolean completed) { this.completed = completed; }
}
