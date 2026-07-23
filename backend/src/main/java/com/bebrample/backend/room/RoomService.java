package com.bebrample.backend.room;

import com.bebrample.backend.common.exception.ResourcesNotFoundException;
import com.bebrample.backend.common.exception.RoomException;
import com.bebrample.backend.user.entity.*;
import com.bebrample.backend.user.UserRepository;
import jakarta.persistence.Lob;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
    // TODO: match history. make 1 match cache and add values to FEN field.
@Service
@RequiredArgsConstructor
public class RoomService {
    private final UserRepository userRepository;
    private final LobbyParticipantMapper lobbyParticipantMapper;
    private final CacheManager cacheManager;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public Room createRoom(){

        LobbyParticipant user = getUserFromContext();

        Cache playerCache = cacheManager.getCache("PLAYER_SESSION_CACHE");
        String player = playerCache.get(user.getId(), String.class);
        if(player != null) throw new RoomException("You already playing!");

        UserLobbyDto dto = lobbyParticipantMapper.toUserLobbyDto(user);
        Room room = new Room();
        room.setFirstPlayer(dto);
        room.setSecondPlayer(null);
        room.setRoomState(null);

        playerCache.put(user.getId(), room.getUuid());
        Cache roomCache = cacheManager.getCache("ROOM_CACHE");
        roomCache.put(room.getUuid(), room);
        return room;
    }
    public Room connectToRoom(String roomUuid){

        LobbyParticipant user = getUserFromContext();


        Cache playerCache = cacheManager.getCache("PLAYER_SESSION_CACHE");
        String player = playerCache.get(user.getId(), String.class);
        if (player != null) throw new RoomException("You already playing!");

        Cache roomCache = cacheManager.getCache("ROOM_CACHE");
        Room room = roomCache.get(roomUuid, Room.class);


        UserLobbyDto dto = lobbyParticipantMapper.toUserLobbyDto(user);

            room.setSecondPlayer(dto);
            roomCache.put(room.getUuid(), room);
            playerCache.put(user.getId(), room.getUuid());
            return room;
    }

        private LobbyParticipant getUserFromContext(){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return (LobbyParticipant) auth.getPrincipal();
        }



}
