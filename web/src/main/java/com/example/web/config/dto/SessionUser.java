package com.example.web.config.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor(staticName = "of")
public class SessionUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userName;
    private final long userNo;
    private final String name;
    private final String email;
}
