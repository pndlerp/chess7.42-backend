package com.bebrample.backend.chess;

import com.bebrample.backend.common.exception.RoomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
// робиться запит на легальність ходу, якщо хід легальний, повертаємо fen і фронтенд сам змінює дошку
@Service
@RequiredArgsConstructor
public class ChessService {
    private final RestClient restClient;

    public MakeMoveResponseDto makeMove(String fen, String move){
        try {
            return restClient.post()
                    .uri("make-move")
                    .body(new MakeMoveRequestDto(fen, move))
                    .retrieve()
                    .body(MakeMoveResponseDto.class);
        }
        catch (HttpClientErrorException.BadRequest exception){
            throw new RoomException("Illegal Move" + move);
        }
    }
}
