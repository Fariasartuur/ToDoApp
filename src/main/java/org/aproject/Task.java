package org.aproject;

import java.time.LocalDate;

public class Task {

    private int id;
    private String name;
    private String description;
    private LocalDate end_Date;
    private boolean status;

    public Task(int task_id, String task_name, String task_description, LocalDate task_end_Date, boolean task_status) {
        this.name = task_name;
        this.description = task_description;
        this.end_Date = task_end_Date;
        this.id = task_id;
        this.status = task_status;
    }

    public LocalDate getEnd_Date() {
        return end_Date;
    }

    public void setEnd_Date(LocalDate end_Date) {
        this.end_Date = end_Date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Task{" +
                "task_id=" + id +
                ", task_name='" + name + '\'' +
                ", task_description='" + description + '\'' +
                ", task_status=" + status +
                '}';
    }
}
