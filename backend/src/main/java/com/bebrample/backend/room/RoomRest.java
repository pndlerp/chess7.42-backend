package com.bebrample.backend.room;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public Long createGame(){
            return roomService.createRoom();
    }

    @PostMapping("{id}")
    public Room connectToRoom(@PathVariable Long id){
        return roomService.connectToRoom(id);
    }
}
