package com.bebrample.backend.user.dto;

import com.bebrample.backend.entity.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLobbyDto {
    private Long id;
    private String username;
    private Color Color;
}