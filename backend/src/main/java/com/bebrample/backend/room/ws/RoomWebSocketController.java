package com.bebrample.backend.room.ws;

import com.bebrample.backend.room.ws.dto.MoveDto;
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
    @MessageMapping("/rooms/{roomId}/move")
    public void makeMove(@DestinationVariable Long roomId, MoveDto moveDto) {
        // 1. Отримуємо хід від клієнта (Spring сам розпарсить JSON у Java-об'єкт)
        System.out.println("Гравець зробив хід у клітинку: " + moveDto);

        // 2. Тут може бути якась перевірка логіки...

        simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId, moveDto);

    }
}
