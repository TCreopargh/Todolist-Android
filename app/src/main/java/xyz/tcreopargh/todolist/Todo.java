package xyz.tcreopargh.todolist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Todo implements Serializable {

    private Calendar notificationTime;
    private Calendar startTime;
    private Calendar deadline;
    private String title;
    private List<SubItem> subItems;
    private boolean isCompleted = false;
    private boolean isUrgent = false;
    private boolean isImportant = false;

    public Todo(Calendar notificationTime,Calendar startTime, Calendar deadline, String title,
        List<SubItem> subItems) {
        this.notificationTime = notificationTime;
        this.startTime = startTime;
        this.deadline = deadline;
        this.title = title;
        this.subItems = subItems;
        subItems = new ArrayList<>();
    }

    public Todo(String title) {
        this.title = title;
        subItems = new ArrayList<>();
    }

    public Calendar getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(Calendar notificationTime) {
        this.notificationTime = notificationTime;
    }

    public Calendar getDeadline() {
        return deadline;
    }

    public void setDeadline(Calendar deadline) {
        this.deadline = deadline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<SubItem> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<SubItem> subItems) {
        this.subItems = subItems;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void toggleCompleted() {
        isCompleted = !isCompleted;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }
}
