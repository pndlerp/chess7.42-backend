package com.bebrample.backend.room;

import com.bebrample.backend.user.UserMapperImpl;
import com.bebrample.backend.user.entity.LobbyParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomRest {
    private final RoomService roomService;

    @PostMapping
    public Room createGame(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LobbyParticipant participant = (LobbyParticipant) auth.getPrincipal();
            return roomService.createRoom(participant);
    }

    @PostMapping("{uuid}")
    public Room connectToRoom(@PathVariable String uuid){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LobbyParticipant participant = (LobbyParticipant) auth.getPrincipal();
        return roomService.connectToRoom(uuid, participant);
    }
}
