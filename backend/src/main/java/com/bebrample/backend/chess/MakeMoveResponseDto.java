package com.bebrample.backend.chess;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Getter
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MakeMoveResponseDto {
    private String moveStatus;
    private String fen;
    private String sideToMove;
    private String gameStatus;
    private List<String> legalMoves;
}
