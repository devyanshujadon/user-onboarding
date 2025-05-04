package com.useronboarding.platform.controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.useronboarding.platform.dto.PostDTO;
import com.useronboarding.platform.model.Post;
import com.useronboarding.platform.model.User;
import com.useronboarding.platform.payload.request.PostRequest;
import com.useronboarding.platform.payload.response.MessageResponse;
import com.useronboarding.platform.repository.PostRepository;
import com.useronboarding.platform.repository.UserRepository;
import com.useronboarding.platform.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        try {
            logger.info("Fetching all posts");
            List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
            List<PostDTO> postDTOs = posts.stream()
                    .map(PostDTO::fromEntity)
                    .collect(Collectors.toList());
            logger.info("Found {} posts", posts.size());
            return ResponseEntity.ok(postDTOs);
        } catch (Exception e) {
            logger.error("Error fetching posts", e);
            return ResponseEntity.status(500).body(new MessageResponse("Error fetching posts: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {
        try {
            logger.info("Fetching post with id {}", id);
            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found with id " + id));
            PostDTO postDTO = PostDTO.fromEntity(post);
            return ResponseEntity.ok(postDTO);
        } catch (Exception e) {
            logger.error("Error fetching post with id {}", id, e);
            return ResponseEntity.status(500).body(new MessageResponse("Error fetching post: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPostsByUser(@PathVariable Long userId) {
        try {
            logger.info("Fetching posts for user with id {}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
            List<Post> posts = postRepository.findByUserOrderByCreatedAtDesc(user);
            List<PostDTO> postDTOs = posts.stream()
                    .map(PostDTO::fromEntity)
                    .collect(Collectors.toList());
            logger.info("Found {} posts for user {}", posts.size(), userId);
            return ResponseEntity.ok(postDTOs);
        } catch (Exception e) {
            logger.error("Error fetching posts for user {}", userId, e);
            return ResponseEntity.status(500).body(new MessageResponse("Error fetching user posts: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPost(@Valid @RequestBody PostRequest postRequest) {
        try {
            logger.info("Creating new post");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            User currentUser = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Post post = new Post();
            post.setContent(postRequest.getContent());
            post.setUser(currentUser);

            postRepository.save(post);
            logger.info("Post created successfully by user {}", userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Post created successfully!"));
        } catch (Exception e) {
            logger.error("Error creating post", e);
            return ResponseEntity.status(500).body(new MessageResponse("Error creating post: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @Valid @RequestBody PostRequest postRequest) {
        try {
            logger.info("Updating post with id {}", id);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found with id " + id));

            if (!post.getUser().getId().equals(userDetails.getId())) {
                logger.warn("User {} attempted to update post {} belonging to user {}",
                        userDetails.getId(), id, post.getUser().getId());
                return ResponseEntity.badRequest().body(new MessageResponse("Not authorized to update this post"));
            }

            post.setContent(postRequest.getContent());
            postRepository.save(post);
            logger.info("Post {} updated successfully by user {}", id, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Post updated successfully!"));
        } catch (Exception e) {
            logger.error("Error updating post with id {}", id, e);
            return ResponseEntity.status(500).body(new MessageResponse("Error updating post: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            logger.info("Deleting post with id {}", id);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found with id " + id));

            if (!post.getUser().getId().equals(userDetails.getId())) {
                logger.warn("User {} attempted to delete post {} belonging to user {}",
                        userDetails.getId(), id, post.getUser().getId());
                return ResponseEntity.badRequest().body(new MessageResponse("Not authorized to delete this post"));
            }

            postRepository.delete(post);
            logger.info("Post {} deleted successfully by user {}", id, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Post deleted successfully!"));
        } catch (Exception e) {
            logger.error("Error deleting post with id {}", id, e);
            return ResponseEntity.status(500).body(new MessageResponse("Error deleting post: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> likePost(@PathVariable Long id) {
        try {
            logger.info("Processing like for post with id {}", id);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            User currentUser = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found with id " + id));

            List<User> likes = post.getLikes();
            boolean wasLiked = likes.contains(currentUser);

            if (wasLiked) {
                likes.remove(currentUser);
                logger.info("User {} unliked post {}", userDetails.getId(), id);
            } else {
                likes.add(currentUser);
                logger.info("User {} liked post {}", userDetails.getId(), id);
            }

            post.setLikes(likes);
            postRepository.save(post);

            String message = wasLiked ? "Post unliked successfully!" : "Post liked successfully!";
            return ResponseEntity.ok(new MessageResponse(message));
        } catch (Exception e) {
            logger.error("Error processing like for post with id {}", id, e);
            return ResponseEntity.status(500).body(new MessageResponse("Error processing like: " + e.getMessage()));
        }
    }
}