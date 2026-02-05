package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String courseName;
    private String teacherName;
    private int rating;
    private String comment;

    @Column
    private LocalDateTime createdAt;

    // ログインユーザーとの紐づけ
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Review() {
    }

    public Review(String courseName, String teacherName, int rating, String comment) {
        this.courseName = courseName;
        this.teacherName = teacherName;
        this.rating = rating;
        this.comment = comment;
    }

    public Integer getId() {
        return id;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setUser(User user) {
        this.user = user;
    }
    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    // 既存フィールドの下あたりに追加
    private String imagePath;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    private int likes;

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public void incrementLikes() { this.likes++; }

}
