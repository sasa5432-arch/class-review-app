package com.example.demo;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final ReviewRepository repository;
    private final CommentRepository commentRepository;

    public ReviewService(ReviewRepository repository,
                         CommentRepository commentRepository) {
        this.repository = repository;
        this.commentRepository = commentRepository;
    }

    // =========================
    // 検索 ＋ 並び替え
    // =========================
    public List<Review> search(String keyword, String target, String sortKey) {
        Sort sort = createSort(sortKey);

        // キーワード無し → 全件
        if (keyword == null || keyword.isBlank()) {
            return repository.findAll(sort);
        }

        // 対象によって分岐
        switch (target) {
            case "course":
                return repository.findByCourseNameContainingIgnoreCase(keyword, sort);
            case "teacher":
                return repository.findByTeacherNameContainingIgnoreCase(keyword, sort);
            default: // all
                return repository
                        .findByCourseNameContainingIgnoreCaseOrTeacherNameContainingIgnoreCase(
                                keyword, keyword, sort);
        }
    }

    // 並び替え条件
    private Sort createSort(String sortKey) {
        return switch (sortKey) {
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating"); // 評価順
            case "likes"  -> Sort.by(Sort.Direction.DESC, "likes");  // いいね順
            default       -> Sort.by(Sort.Direction.DESC, "id");     // 新しい順
        };
    }

    // =========================
    // 基本的な CRUD
    // =========================
    public void addReview(Review review) {
        repository.save(review);
    }

    public void save(Review review) {
        repository.save(review);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public Review getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found: " + id));
    }

    // =========================
    // いいね
    // =========================
    public void like(Integer id) {
        Review r = getById(id);
        r.incrementLikes();
        repository.save(r);
    }

    // =========================
    // 平均値計算
    // =========================
    public double getAverage(String courseName) {
        List<Review> list = repository.findByCourseNameContainingIgnoreCase(courseName);
        if (list.isEmpty()) return 0;

        int sum = 0;
        for (Review r : list) {
            sum += r.getRating();
        }
        return (double) sum / list.size();
    }

    // =========================
    // コメント関連
    // =========================
    public List<Comment> getComments(Review review) {
        return commentRepository.findByReviewAndParentCommentIsNullOrderByIdAsc(review);
    }

    public void addComment(Review review, User user, String content, String university, String faculty, String department) {
        Comment c = new Comment(review, user, content, university, faculty, department);
        commentRepository.save(c);
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + id));
    }

    public void addReply(Review review, Comment parent, User user, String content, String university, String faculty, String department) {
        Comment c = new Comment(review, user, content, university, faculty, department);
        c.setParentComment(parent);
        commentRepository.save(c);
    }

    // =========================
    // 絞り込み用（授業名・教員名・ユーザー）
    // =========================
    public List<Review> getByCourseName(String courseName) {
        return repository.findByCourseName(courseName);
    }

    public List<Review> getByTeacherName(String teacherName) {
        return repository.findByTeacherName(teacherName);
    }

    public List<Review> getByUser(User user) {
        return repository.findByUser(user);
    }
    public List<Review> getAll() {
        return repository.findAll();
    }

}
