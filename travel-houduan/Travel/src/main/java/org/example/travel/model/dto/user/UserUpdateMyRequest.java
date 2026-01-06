package org.example.travel.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateMyRequest implements Serializable {
    private String userName;
    private String userProfile;
    private String userAvatar;
    private String email;

    private static final long serialVersionUID = 1L;
}
