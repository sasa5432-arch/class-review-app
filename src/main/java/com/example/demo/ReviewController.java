package com.example.demo;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.UUID;


@Controller
public class ReviewController {

    private final ReviewService service;
    private final UserRepository userRepository;

    public ReviewController(ReviewService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    // ▼ 追加：現在ログイン中の User を取得するメソッド
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // username = email
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    // 一覧表示
    @GetMapping("/reviews")
    public String listReviews(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "target", defaultValue = "all") String target,
            @RequestParam(name = "sort", defaultValue = "recent") String sort,
            Model model
    ) {
        List<Review> reviews = service.search(keyword, target, sort);

        model.addAttribute("reviews", reviews);
        model.addAttribute("keyword", keyword);
        model.addAttribute("target", target);
        model.addAttribute("sort", sort);

        return "reviews";
    }



    // 投稿フォーム
    @GetMapping("/reviews/new")
    public String showForm() {
        return "form";
    }

    // 投稿処理
    @PostMapping("/reviews")
    public String addReview(
            @RequestParam String courseName,
            @RequestParam String teacherName,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile
    ) {
        User user = getCurrentUser();

        Review review = new Review(courseName, teacherName, rating, comment);
        review.setUser(user);

        // ★ 画像があれば保存。エラーが出てもレビューは保存する。
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imagePath = saveImage(imageFile);
                review.setImagePath(imagePath);
            } catch (IOException e) {
                e.printStackTrace(); // とりあえずログだけ出して無視
            }
        }

        service.addReview(review);
        return "redirect:/reviews";
    }

    
    // クラスの中に追記
    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Integer id) {
        User current = getCurrentUser();
        Review review = service.getById(id);

        if (!review.getUser().getId().equals(current.getId())) {
            return "redirect:/reviews?forbidden";
        }

        service.deleteById(id);
        return "redirect:/reviews";
    }
    // 編集フォーム表示
    @GetMapping("/reviews/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Review review = service.getById(id);
        model.addAttribute("review", review);
        return "edit";
    }

    // 編集内容の保存
    @PostMapping("/reviews/{id}/edit")
    public String updateReview(
            @PathVariable Integer id,
            @RequestParam String courseName,
            @RequestParam String teacherName,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {

        User current = getCurrentUser();
        Review review = service.getById(id);

        if (!review.getUser().getId().equals(current.getId())) {
            return "redirect:/reviews?forbidden";
        }

        review.setCourseName(courseName);
        review.setTeacherName(teacherName);
        review.setRating(rating);
        review.setComment(comment);

        // 画像が送られてきたときだけ更新
        String newImagePath = saveImage(imageFile);
        if (newImagePath != null) {
            review.setImagePath(newImagePath);
        }

        service.save(review);
        return "redirect:/reviews";
    }

    @GetMapping("/reviews/average")
    public String showAverage(@RequestParam String courseName, Model model) {
        double avg = service.getAverage(courseName);
        model.addAttribute("courseName", courseName);
        model.addAttribute("average", avg);
        return "average";
    }
    @GetMapping("/me")
    public String myPage(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("reviews", service.getByUser(user));
        return "me";
    }
    private String saveImage(MultipartFile imageFile) throws IOException {
        // ファイルが空なら何もしない
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        // プロジェクト直下に uploads フォルダを作る
        Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        // 元ファイル名から拡張子だけもらう
        String originalFilename = imageFile.getOriginalFilename();
        String ext = "";
        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf(".");
            if (dot != -1) {
                ext = originalFilename.substring(dot); // .png, .jpg など
            }
        }

        // ランダムなファイル名を作成
        String filename = UUID.randomUUID().toString() + ext;

        // 保存先パス
        Path target = uploadDir.resolve(filename);

        // ファイルを保存
        imageFile.transferTo(target.toFile());

        // ブラウザからアクセスするときのパスを返す
        return "/uploads/" + filename;
    }

    @PostMapping("/reviews/{id}/like")
    public String likeReview(@PathVariable Integer id) {
        service.like(id);
        return "redirect:/reviews";
    }
    // レビュー詳細＆コメント一覧
    @GetMapping("/reviews/{id}")
    public String showDetail(@PathVariable Integer id, Model model) {
        Review review = service.getById(id);
        model.addAttribute("review", review);
        model.addAttribute("comments", service.getComments(review));
        return "review-detail";
    }

    // コメント投稿
    @PostMapping("/reviews/{id}/comments")
    public String addComment(@PathVariable Integer id,
                             @RequestParam String content,
                             @RequestParam String university,
                             @RequestParam String faculty,
                             @RequestParam String department) {
        User user = getCurrentUser();
        Review review = service.getById(id);
        service.addComment(review, user, content, university, faculty, department);
        return "redirect:/reviews/" + id;
    }

    // コメントへの返信
    @PostMapping("/reviews/{id}/comments/{commentId}/replies")
    public String addReply(@PathVariable Integer id,
                           @PathVariable Long commentId,
                           @RequestParam String content,
                           @RequestParam String university,
                           @RequestParam String faculty,
                           @RequestParam String department) {
        User user = getCurrentUser();
        Review review = service.getById(id);
        Comment parent = service.getCommentById(commentId);
        if (!parent.getReview().getId().equals(review.getId())) {
            return "redirect:/reviews/" + id;
        }
        service.addReply(review, parent, user, content, university, faculty, department);
        return "redirect:/reviews/" + id;
    }
    @GetMapping("/reviews/by-course")
    public String byCourse(@RequestParam String name, Model model) {
        model.addAttribute("reviews", service.getByCourseName(name));
        model.addAttribute("filterTitle", "授業名: " + name);
        return "reviews-filter";
    }

    @GetMapping("/reviews/by-teacher")
    public String byTeacher(@RequestParam String name, Model model) {
        model.addAttribute("reviews", service.getByTeacherName(name));
        model.addAttribute("filterTitle", "教員: " + name);
        return "reviews-filter";
    }

}
