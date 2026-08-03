package com.bebrample.backend.chess;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MakeMoveRequestDto {
    private final String fen;
    private final String move;
}
