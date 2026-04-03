package com.cinebuzz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponseDto {

    private String token;
    /** Database id of the authenticated user (for client display / debugging; API auth still uses JWT). */
    private Long userId;
    private String name;
    private String email;
    private String role;
}