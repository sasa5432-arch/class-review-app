package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @Column
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Comment> replies = new ArrayList<>();

    @Column(nullable = false, length = 100)
    private String university;

    @Column(nullable = false, length = 100)
    private String faculty;

    @Column(nullable = false, length = 100)
    private String department;

    public Comment() {}

    public Comment(Review review, User user, String content, String university, String faculty, String department) {
        this.review = review;
        this.user = user;
        this.content = content;
        this.university = university;
        this.faculty = faculty;
        this.department = department;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public Review getReview() { return review; }
    public User getUser() { return user; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Comment getParentComment() { return parentComment; }
    public List<Comment> getReplies() { return replies; }
    public String getUniversity() { return university; }
    public String getFaculty() { return faculty; }
    public String getDepartment() { return department; }

    public void setReview(Review review) { this.review = review; }
    public void setUser(User user) { this.user = user; }
    public void setContent(String content) { this.content = content; }
    public void setParentComment(Comment parentComment) { this.parentComment = parentComment; }
    public void setUniversity(String university) { this.university = university; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public void setDepartment(String department) { this.department = department; }
}
