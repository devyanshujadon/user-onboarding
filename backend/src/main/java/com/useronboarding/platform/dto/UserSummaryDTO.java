package com.useronboarding.platform.dto;

import com.useronboarding.platform.model.User;

import lombok.Data;

@Data
public class UserSummaryDTO {
    private Long id;
    private String username;
    private String profilePicture;

    public static UserSummaryDTO fromUser(User user) {
        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setProfilePicture(user.getProfilePicture());
        return dto;
    }
}