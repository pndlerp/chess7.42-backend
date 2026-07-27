package com.bebrample.backend.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    public List<Match> findMatchesByBlackPlayer_IdOrWhitePlayer_Id(Long id, Long id2);
}
