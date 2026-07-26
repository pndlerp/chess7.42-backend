package com.bebrample.backend.room.ws;

import com.bebrample.backend.room.RoomService;
import com.bebrample.backend.room.ws.dto.MoveDto;
import com.bebrample.backend.room.ws.dto.WebSocketEvent;
import com.bebrample.backend.user.entity.LobbyParticipant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RoomWebSocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoomService roomService;
    @MessageMapping("/rooms/{uuid}/move")
    public void makeMove(@DestinationVariable String uuid, MoveDto move, Principal principal) {

        if(principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "are you even authorized?");

        Authentication auth = (Authentication) principal;
        LobbyParticipant user = (LobbyParticipant) auth.getPrincipal();
        roomService.updateChessState(uuid, move, user);
        log.info("uuid: {}", uuid);
    }

    public void sendConnectionMessage(@DestinationVariable String uuid){
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + uuid, "user connected!");
    }
}
