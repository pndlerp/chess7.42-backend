package com.bebrample.backend.room.ws;

import com.bebrample.backend.common.exception.RoomException;
import com.bebrample.backend.room.RedisLockManager;
import com.bebrample.backend.room.RoomService;
import com.bebrample.backend.room.ws.dto.MoveDto;
import com.bebrample.backend.user.entity.LobbyParticipant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Duration;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RoomWebSocketController {
    private final RoomService roomService;
    private final RedisLockManager redisLockManager;
    @MessageMapping("/rooms/{uuid}/move")
    public void makeMove(@DestinationVariable String uuid, MoveDto move, Principal principal) {

        String lockKey = "room:state:" + uuid;

        String lockId = redisLockManager.tryLock(lockKey, Duration.ofSeconds(5));
        if(lockId == null) throw new RoomException("You making moves very fast! wait a little");

        if(principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "are you even authorized?");

        Authentication auth = (Authentication) principal;
        LobbyParticipant user = (LobbyParticipant) auth.getPrincipal();
        try {
            roomService.updateChessState(uuid, move, user);
            log.info("move was made");
        } finally {
            redisLockManager.unlockLock(lockKey, lockId);
        }
    }
}
