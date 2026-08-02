package com.bebrample.backend.room.ws;

import com.bebrample.backend.common.exception.ResourcesNotFoundException;
import com.bebrample.backend.entity.Role;
import com.bebrample.backend.room.RedisRepository;
import com.bebrample.backend.room.Room;
import com.bebrample.backend.room.ws.dto.ConnectDto;
import com.bebrample.backend.room.ws.dto.WebSocketEvent;
import com.bebrample.backend.user.entity.LobbyParticipant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomWebSocketListener {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RedisRepository redisRepository;
    private static final String destinationPrefix = "/topic/rooms/";
    @EventListener
    public void onConnect(SessionSubscribeEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        var sessionAttributes = accessor.getSessionAttributes();
        String roomUuid = null;
        if (destination != null && sessionAttributes != null) {
            log.info("Клієнт підписався на: {}", destination);
            sessionAttributes.put("destination", destination);
            roomUuid = destination.substring(destinationPrefix.length());

        } else {
            log.info("не вдалось отримати destination");
            throw new ResourcesNotFoundException("не вдалось отримати destination");
        }
        LobbyParticipant participant = getLobbyParticipant(accessor);
        Room room = redisRepository.getRoom(roomUuid);
        if(room == null) throw new ResourcesNotFoundException("this Room isn't accessible anymore");
        
        ConnectDto dto = getConnectDto(participant, room);
        simpMessagingTemplate.convertAndSend(destination, new WebSocketEvent<>("CONNECTED", dto));
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        var sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null) {
            String destination = (String) sessionAttributes.get("destination");
            String roomUuid = destination.substring(destinationPrefix.length());


            LobbyParticipant participant = getLobbyParticipant(accessor);
            Room room = redisRepository.getRoom(roomUuid);
            if(room == null) throw new ResourcesNotFoundException("this Room isn't accessible anymore");
            ConnectDto dto = getConnectDto(participant, room);
            simpMessagingTemplate.convertAndSend(destination, new WebSocketEvent<>("DISCONNECTED", dto));
        }
    }

    private static ConnectDto getConnectDto(LobbyParticipant participant, Room room) {
        ConnectDto dto = new ConnectDto();
        dto.setPlayerName(participant.getUsername());
        if(Objects.equals(room.getFirstPlayer().getId(), participant.getId()) || Objects.equals(room.getSecondPlayer().getId(),participant.getId()))
            dto.setRole(Role.PLAYER);
        else dto.setRole(Role.SPECTATOR);

        dto.setRoomState(room.getRoomState());
        if(room.getSecondPlayer() != null) {
            if (Objects.equals(room.getFirstPlayer().getUsername(), participant.getUsername()))
                dto.setOpponentName(room.getSecondPlayer().getUsername());
            else dto.setOpponentName(room.getFirstPlayer().getUsername());
        }
        return dto;
    }

    private static LobbyParticipant getLobbyParticipant(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        return (LobbyParticipant) auth.getPrincipal();
    }
}
