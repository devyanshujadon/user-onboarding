package com.useronboarding.platform.dto;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.useronboarding.platform.model.Post;

import lombok.Data;

@Data
public class PostDTO {
    private Long id;
    private String content;
    private UserSummaryDTO user;
    private Date createdAt;
    private List<UserSummaryDTO> likes;
    private int commentCount;

    public static PostDTO fromEntity(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());

        if (post.getUser() != null) {
            dto.setUser(UserSummaryDTO.fromUser(post.getUser()));
        }

        if (post.getLikes() != null) {
            dto.setLikes(post.getLikes().stream()
                    .map(UserSummaryDTO::fromUser)
                    .collect(Collectors.toList()));
        }

        if (post.getComments() != null) {
            dto.setCommentCount(post.getComments().size());
        }

        return dto;
    }
}