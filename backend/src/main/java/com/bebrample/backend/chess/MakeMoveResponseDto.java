package com.bebrample.backend.chess;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MakeMoveResponseDto {
    private String fen;
    private String status;
    private String winner;
}
