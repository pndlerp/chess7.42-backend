package com.bebrample.backend.room.ws;

import com.bebrample.backend.common.exception.ResourcesNotFoundException;
import com.bebrample.backend.entity.Role;
import com.bebrample.backend.room.Room;
import com.bebrample.backend.room.RoomService;
import com.bebrample.backend.room.ws.dto.ConnectDto;
import com.bebrample.backend.room.ws.dto.WebSocketEvent;
import com.bebrample.backend.user.entity.LobbyParticipant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomWebSocketListener {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RedisTemplate<String, Room> redisTemplate;
    private final RedisTemplate<String, String> redisUserTemplate;
    private final RoomService roomService;
    private static final String destinationPrefix = "/room/topic/";
    @EventListener
    public void onConnect(SessionSubscribeEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
//        roomService.createRoom();
        String roomUuid = destination.substring(destinationPrefix.length());
        Principal principal = accessor.getUser();
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        LobbyParticipant participant = (LobbyParticipant) auth.getPrincipal();
        //String roomUuid = redisUserTemplate.opsForValue().get("user:" + participant.getId());
        Room room = redisTemplate.opsForValue().get("room:state:"+ roomUuid);
        if(room == null) throw new ResourcesNotFoundException("this Room isn't accessible anymore");
        ConnectDto dto = new ConnectDto();
        dto.setPlayerName(participant.getUsername());
        if(room.getFirstPlayer().getId().equals(participant.getId()) || room.getSecondPlayer().getId().equals(participant.getId()))
            dto.setRole(Role.PLAYER);
        else dto.setRole(Role.SPECTATOR);

        dto.setRoomState(room.getRoomState());
        if(room.getSecondPlayer() != null) {
            if (room.getFirstPlayer().getUsername().equals(participant.getUsername()))
                dto.setOpponentName(room.getSecondPlayer().getUsername());
            else dto.setOpponentName(room.getFirstPlayer().getUsername());
        }

//        log.info("{}", destination);
//        redisTemplate.opsForValue().get(roomUuid);

        simpMessagingTemplate.convertAndSend(destination, new WebSocketEvent<>("CONNECTED", dto));
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());


    }
}
