package com.example.exam;

public class Task {
    private String text;
    private boolean done;
    private String dueDate; // ISO format, e.g. 2024-06-01
    private String category;
    private String priority; // High, Medium, Low
    private String displayDueDate; // For localized display (e.g., Bengali numerals)

    public Task(String text, boolean done, String dueDate, String category, String priority) {
        this.text = text;
        this.done = done;
        this.dueDate = dueDate;
        this.category = category;
        this.priority = priority != null ? priority : "Medium";
    }

    // For backward compatibility
    public Task(String text, boolean done, String dueDate, String category) {
        this(text, done, dueDate, category, "Medium");
    }

    public Task(String text, boolean done) {
        this(text, done, null, null, "Medium");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDisplayDueDate() {
        // Always return the original date format, regardless of language
        return dueDate;
    }

    public void setDisplayDueDate(String displayDueDate) {
        this.displayDueDate = displayDueDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
} 