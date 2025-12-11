package be.project.model;

import java.time.LocalDateTime;

public class SharedWishlist {

    private int id;
    private LocalDateTime sharedAt;  
    private String notification;         

    public SharedWishlist() {
    }

    public SharedWishlist(int id, LocalDateTime sharedAt, String notification) {
        this.id = id;
        this.sharedAt = sharedAt;
        this.notification = notification;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(LocalDateTime sharedAt) {
        this.sharedAt = sharedAt;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    @Override
    public String toString() {
        return "SharedWishlist{" +
                "id=" + id +
                ", sharedAt=" + sharedAt +
                ", notification='" + notification + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedWishlist that = (SharedWishlist) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}