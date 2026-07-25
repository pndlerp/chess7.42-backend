package com.bebrample.backend.room.ws;

import com.bebrample.backend.room.RoomService;
import com.bebrample.backend.room.ws.dto.MoveDto;
import com.bebrample.backend.room.ws.dto.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class RoomWebSocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoomService roomService;
    @MessageMapping("/rooms/{uuid}/move")
    public void makeMove(@DestinationVariable String uuid, MoveDto move) {
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + uuid, new WebSocketEvent<>("MOVE",move));

        roomService.updateChessState(uuid, move);
    }

    public void sendConnectionMessage(@DestinationVariable String uuid){
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + uuid, "user connected!");
    }
}
