package org.example.travel.model.dto.user;


import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdatePasswordRequest implements Serializable {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    private static final long serialVersionUID = 1L;
}
