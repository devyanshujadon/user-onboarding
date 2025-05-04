package com.useronboarding.platform.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.useronboarding.platform.model.Comment;
import com.useronboarding.platform.model.Post;
import com.useronboarding.platform.model.User;
import com.useronboarding.platform.payload.request.CommentRequest;
import com.useronboarding.platform.payload.response.MessageResponse;
import com.useronboarding.platform.repository.CommentRepository;
import com.useronboarding.platform.repository.PostRepository;
import com.useronboarding.platform.repository.UserRepository;
import com.useronboarding.platform.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/comments")
public class CommentController {

        private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

        @Autowired
        private CommentRepository commentRepository;

        @Autowired
        private PostRepository postRepository;

        @Autowired
        private UserRepository userRepository;

        @GetMapping("/post/{postId}")
        public ResponseEntity<?> getCommentsByPost(@PathVariable Long postId) {
                try {
                        logger.info("Fetching comments for post with id {}", postId);
                        Post post = postRepository.findById(postId)
                                        .orElseThrow(() -> new RuntimeException("Post not found with id " + postId));
                        List<Comment> comments = commentRepository
                                        .findByParentCommentIsNullAndPostOrderByCreatedAtDesc(post);
                        logger.info("Found {} comments for post {}", comments.size(), postId);
                        return ResponseEntity.ok(comments);
                } catch (Exception e) {
                        logger.error("Error fetching comments for post with id {}", postId, e);
                        return ResponseEntity.status(500)
                                        .body(new MessageResponse("Error fetching comments: " + e.getMessage()));
                }
        }

        @GetMapping("/{id}/replies")
        public ResponseEntity<?> getReplies(@PathVariable Long id) {
                try {
                        logger.info("Fetching replies for comment with id {}", id);
                        Comment parentComment = commentRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Comment not found with id " + id));
                        List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(parentComment);
                        logger.info("Found {} replies for comment {}", replies.size(), id);
                        return ResponseEntity.ok(replies);
                } catch (Exception e) {
                        logger.error("Error fetching replies for comment with id {}", id, e);
                        return ResponseEntity.status(500)
                                        .body(new MessageResponse("Error fetching replies: " + e.getMessage()));
                }
        }

        @PostMapping("/post/{postId}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> addComment(
                        @PathVariable Long postId,
                        @Valid @RequestBody CommentRequest commentRequest) {
                try {
                        logger.info("Adding comment to post with id {}", postId);
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                        User currentUser = userRepository.findById(userDetails.getId())
                                        .orElseThrow(() -> new RuntimeException("User not found"));

                        Post post = postRepository.findById(postId)
                                        .orElseThrow(() -> new RuntimeException("Post not found with id " + postId));

                        Comment comment = new Comment();
                        comment.setContent(commentRequest.getContent());
                        comment.setUser(currentUser);
                        comment.setPost(post);

                        commentRepository.save(comment);
                        logger.info("Comment added successfully to post {} by user {}", postId, userDetails.getId());
                        return ResponseEntity.ok(new MessageResponse("Comment added successfully!"));
                } catch (Exception e) {
                        logger.error("Error adding comment to post with id {}", postId, e);
                        return ResponseEntity.status(500)
                                        .body(new MessageResponse("Error adding comment: " + e.getMessage()));
                }
        }

        @PostMapping("/{commentId}/reply")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> replyToComment(
                        @PathVariable Long commentId,
                        @Valid @RequestBody CommentRequest commentRequest) {
                try {
                        logger.info("Adding reply to comment with id {}", commentId);
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                        User currentUser = userRepository.findById(userDetails.getId())
                                        .orElseThrow(() -> new RuntimeException("User not found"));

                        Comment parentComment = commentRepository.findById(commentId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Comment not found with id " + commentId));

                        Comment reply = new Comment();
                        reply.setContent(commentRequest.getContent());
                        reply.setUser(currentUser);
                        reply.setPost(parentComment.getPost());
                        reply.setParentComment(parentComment);

                        commentRepository.save(reply);
                        logger.info("Reply added successfully to comment {} by user {}", commentId,
                                        userDetails.getId());
                        return ResponseEntity.ok(new MessageResponse("Reply added successfully!"));
                } catch (Exception e) {
                        logger.error("Error adding reply to comment with id {}", commentId, e);
                        return ResponseEntity.status(500)
                                        .body(new MessageResponse("Error adding reply: " + e.getMessage()));
                }
        }

        @PutMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> updateComment(
                        @PathVariable Long id,
                        @Valid @RequestBody CommentRequest commentRequest) {
                try {
                        logger.info("Updating comment with id {}", id);
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                        Comment comment = commentRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Comment not found with id " + id));

                        if (!comment.getUser().getId().equals(userDetails.getId())) {
                                logger.warn("User {} attempted to update comment {} belonging to user {}",
                                                userDetails.getId(), id, comment.getUser().getId());
                                return ResponseEntity.badRequest()
                                                .body(new MessageResponse("Not authorized to update this comment"));
                        }

                        comment.setContent(commentRequest.getContent());
                        commentRepository.save(comment);
                        logger.info("Comment {} updated successfully by user {}", id, userDetails.getId());
                        return ResponseEntity.ok(new MessageResponse("Comment updated successfully!"));
                } catch (Exception e) {
                        logger.error("Error updating comment with id {}", id, e);
                        return ResponseEntity.status(500)
                                        .body(new MessageResponse("Error updating comment: " + e.getMessage()));
                }
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> deleteComment(@PathVariable Long id) {
                try {
                        logger.info("Deleting comment with id {}", id);
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                        Comment comment = commentRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Comment not found with id " + id));

                        if (!comment.getUser().getId().equals(userDetails.getId())) {
                                logger.warn("User {} attempted to delete comment {} belonging to user {}",
                                                userDetails.getId(), id, comment.getUser().getId());
                                return ResponseEntity.badRequest()
                                                .body(new MessageResponse("Not authorized to delete this comment"));
                        }

                        commentRepository.delete(comment);
                        logger.info("Comment {} deleted successfully by user {}", id, userDetails.getId());
                        return ResponseEntity.ok(new MessageResponse("Comment deleted successfully!"));
                } catch (Exception e) {
                        logger.error("Error deleting comment with id {}", id, e);
                        return ResponseEntity.status(500)
                                        .body(new MessageResponse("Error deleting comment: " + e.getMessage()));
                }
        }

        @PostMapping("/{id}/like")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> likeComment(@PathVariable Long id) {
                try {
                        logger.info("Processing like for comment with id {}", id);
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                        User currentUser = userRepository.findById(userDetails.getId())
                                        .orElseThrow(() -> new RuntimeException("User not found"));

                        Comment comment = commentRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Comment not found with id " + id));

                        List<User> likes = comment.getLikes();
                        boolean wasLiked = likes.contains(currentUser);

                        if (wasLiked) {
                                likes.remove(currentUser);
                                logger.info("User {} unliked comment {}", userDetails.getId(), id);
                        } else {
                                likes.add(currentUser);
                                logger.info("User {} liked comment {}", userDetails.getId(), id);
                        }

                        comment.setLikes(likes);
                        commentRepository.save(comment);

                        String message = wasLiked ? "Comment unliked successfully!" : "Comment liked successfully!";
                        return ResponseEntity.ok(new MessageResponse(message));
                } catch (Exception e) {
                        logger.error("Error processing like for comment with id {}", id, e);
                        return ResponseEntity.status(500)
                                        .body(new MessageResponse("Error processing like: " + e.getMessage()));
                }
        }
}