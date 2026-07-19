package com.bebrample.backend.room;

import com.bebrample.backend.common.exception.ResourcesNotFoundException;
import com.bebrample.backend.common.exception.RoomException;
import com.bebrample.backend.user.User;
import com.bebrample.backend.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    private final SimpMessagingTemplate simpMessagingTemplate;
    @Transactional
    public RoomResponseDto createRoom(){
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByUsername(currentUsername).orElseThrow(() ->
                    new ResourcesNotFoundException("i dunno how this could happened"));

        Cache playerCache = cacheManager.getCache("PLAYER_SESSION_CACHE");
        String player = playerCache.get(user.getId(), String.class);
        if(player != null) throw new RoomException("You already playing!");

        Room room = new Room();
        room.setFirstPlayer(user);
        room.setSecondPlayer(null);
        room.setStatus(null);
        RoomResponseDto dto = toDto(room);

        playerCache.put(user.getId(), room.getUuid());
        Cache roomCache = cacheManager.getCache("ROOM_CACHE");
        roomCache.put(dto.getUuid(), dto);
        return dto;
    }
    @Transactional
    public RoomResponseDto connectToRoom(String roomUuid){

        Cache roomCache = cacheManager.getCache("ROOM_CACHE");
        RoomResponseDto room = roomCache.get(roomUuid, RoomResponseDto.class);

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByUsername(currentUsername).orElseThrow(() ->
                new ResourcesNotFoundException("i dunno how this could happened"));

        Cache playerCache = cacheManager.getCache("PLAYER_SESSION_CACHE");
        String player = playerCache.get(user.getId(), String.class);
        if(player != null) throw new RoomException("You already playing!");

        room.setSecondPlayerId(user.getId());
        roomCache.put(room.getUuid(), room);
        playerCache.put(user.getId(), room.getUuid());

        simpMessagingTemplate.convertAndSend("/topic/room/" + roomUuid, room);
        return room;
    }

    private RoomResponseDto toDto(Room room){
        RoomResponseDto dto = new RoomResponseDto();

        dto.setUuid(room.getUuid());
        dto.setFirstPlayerId(room.getFirstPlayer().getId());
        if(room.getSecondPlayer() != null) dto.setSecondPlayerId(room.getSecondPlayer().getId());
        return dto;
    }

    public void sendMessage(){
        simpMessagingTemplate.convertAndSend("topic/room/1", "server here");
    }

}
