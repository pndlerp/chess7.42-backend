package com.bebrample.backend.match;

import com.bebrample.backend.match.dto.MatchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MatchRest {
    private final MatchService matchService;

    @GetMapping("player/matches/{id}")
    public List<MatchResponseDto> getPlayerMatches(@PathVariable Long id){
        return matchService.getPlayerMatches(id);
    }
    @GetMapping("matches/{id}")
    public MatchResponseDto getMatch(@PathVariable Long id){
        return matchService.getMatch(id);
    }
}
