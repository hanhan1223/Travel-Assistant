package org.example.travel.model.dto.user;

import lombok.Data;

@Data
public class UserLoginByEmailRequest {

    private static final long serialVersionUID = 8735650154179439661L;

    private String email;

    private String password;
}
