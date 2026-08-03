package com.bebrample.backend.room.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class GameOverDto {
    String winner;
    String reason;
}
