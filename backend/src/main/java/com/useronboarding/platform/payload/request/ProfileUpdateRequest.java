package com.useronboarding.platform.payload.request;

import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {
    @Size(max = 255)
    private String bio;

    @Size(max = 255)
    private String profilePicture;

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}