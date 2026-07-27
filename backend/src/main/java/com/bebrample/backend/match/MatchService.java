package com.bebrample.backend.match;

import com.bebrample.backend.common.exception.ResourcesNotFoundException;
import com.bebrample.backend.entity.Color;
import com.bebrample.backend.match.dto.MatchResponseDto;
import com.bebrample.backend.room.RedisRepository;
import com.bebrample.backend.room.Room;
import com.bebrample.backend.room.ws.dto.MoveDto;
import com.bebrample.backend.user.UserRepository;
import com.bebrample.backend.user.entity.User;
import com.bebrample.backend.user.entity.UserLobbyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    @Async
    public void saveMatch(Room room, List<MoveDto> moves){
        Long whitePlayerId;
        Long blackPlayerId;
        if(room.getFirstPlayer().getColor().equals(Color.WHITE)){
            whitePlayerId = room.getFirstPlayer().getId();
            blackPlayerId = room.getSecondPlayer().getId();
        } else {
             whitePlayerId = room.getSecondPlayer().getId();
             blackPlayerId = room.getFirstPlayer().getId();
        }

        Match match = new Match();
        match.setRoomUuid(room.getUuid());
        User whitePlayer = userRepository.findUserById(whitePlayerId)
                .orElseThrow(() -> new ResourcesNotFoundException("User not found in db"));
        User blackPlayer = userRepository.findUserById(blackPlayerId)
                .orElseThrow(() -> new ResourcesNotFoundException("User not found in db"));
        match.setBlackPlayer(whitePlayer);
        match.setWhitePlayer(blackPlayer);
        match.setFinalFen(null);
        match.setResult(null);
        match.setPgn(generatePgn(moves));

        matchRepository.save(match);
    }

    private String generatePgn(List<MoveDto> moves){
        return moves.stream()
                .map(move -> move.getFrom() + "-" + move.getTo())
                .collect(Collectors.joining(" "));
    }

    public List<MatchResponseDto> getPlayerMatches(Long id){
        List<Match> matches = matchRepository.findMatchesByBlackPlayer_IdOrWhitePlayer_Id(id, id);
        List<MatchResponseDto> matchDtos = new ArrayList<>();
        for(Match m : matches){
            UserLobbyDto whitePlayer = new UserLobbyDto(m.getWhitePlayer().getId(), m.getWhitePlayer().getUsername(), Color.WHITE);
            UserLobbyDto blackPlayer = new UserLobbyDto(m.getBlackPlayer().getId(), m.getBlackPlayer().getUsername(), Color.BLACK);
            MatchResponseDto dto = new MatchResponseDto(m.getId(),m.getRoomUuid(),whitePlayer,blackPlayer, m.getFinalFen(), m.getPgn(), m.getResult());
            matchDtos.add(dto);
        }
        return matchDtos;
    }

    public MatchResponseDto getMatch(Long id){
        Match m = matchRepository.findById(id).orElseThrow(() -> new ResourcesNotFoundException("not found"));
        UserLobbyDto whitePlayer = new UserLobbyDto(m.getWhitePlayer().getId(), m.getWhitePlayer().getUsername(), Color.WHITE);
        UserLobbyDto blackPlayer = new UserLobbyDto(m.getBlackPlayer().getId(), m.getBlackPlayer().getUsername(), Color.BLACK);
        return new MatchResponseDto(m.getId(),m.getRoomUuid(),whitePlayer,blackPlayer, m.getFinalFen(), m.getPgn(), m.getResult());
    }
}
