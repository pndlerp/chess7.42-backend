package com.bebrample.backend.chess;

import com.bebrample.backend.common.exception.RoomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

// робиться запит на легальність ходу, якщо хід легальний, повертаємо fen і фронтенд сам змінює дошку
@Service
@RequiredArgsConstructor
public class ChessService {
    private final RestClient restClient;

    public MakeMoveResponseDto makeMove(String fen, String move){
        System.out.println("FEN: " + fen + ", MOVE: " + move);
        try {
            return restClient.post()
                    .uri("/api/v1/chess/make-move")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new MakeMoveRequestDto(fen, move))
                    .retrieve()
                    .body(MakeMoveResponseDto.class);
        }
        catch (HttpClientErrorException.BadRequest exception){
            throw new RoomException("Internal server Error" );
        }
    }
}
