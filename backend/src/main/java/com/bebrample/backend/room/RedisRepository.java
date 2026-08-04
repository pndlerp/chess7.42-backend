package com.bebrample.backend.room;

import com.bebrample.backend.room.ws.dto.MoveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisRepository {
    private final RedisTemplate<String,Room> redisRoomTemplate;
    private final RedisTemplate<String,String> stringRedisTemplate;
    private final RedisTemplate<String, MoveDto> redisMoveDtoTemplate;

    private static final String CACHE_ROOM_KEY_PREFIX = "room:state:";
    private static final String CACHE_MOVES_KEY_PREFIX = "room:moves:";
    private static final String CACHE_LOBBY_PARTICIPANT_KEY_PREFIX = "user:";

    public void updateUser(String roomUuid, Long participantId){
        stringRedisTemplate.opsForValue()
                .set(CACHE_LOBBY_PARTICIPANT_KEY_PREFIX + participantId,
                        roomUuid, Duration.ofMinutes(24));
    }

    public String getUserRoomUuid(Long userId){
        return stringRedisTemplate.opsForValue()
                .get(CACHE_LOBBY_PARTICIPANT_KEY_PREFIX + userId);
    }

    public void deleteUser(Long userId){
        stringRedisTemplate.delete(CACHE_LOBBY_PARTICIPANT_KEY_PREFIX + userId);
    }

    public List<MoveDto> getMoves(String roomUuid){
        return redisMoveDtoTemplate.opsForList().range(CACHE_MOVES_KEY_PREFIX + roomUuid, 0, -1);
    }

    public boolean isPlaying(Long userId){
        String player = stringRedisTemplate.opsForValue()
                .get(CACHE_LOBBY_PARTICIPANT_KEY_PREFIX + userId);
        return player != null;
    }

    public Room getRoom(String roomUuid){
            return redisRoomTemplate.opsForValue()
                    .get(CACHE_ROOM_KEY_PREFIX + roomUuid);
    }

    public void updateRoom(Room room){
        redisRoomTemplate.opsForValue()
                .set(CACHE_ROOM_KEY_PREFIX + room.getUuid(),
                        room, Duration.ofMinutes(24));
    }

    public boolean isRoomNull(String roomUuid){
        Room room = redisRoomTemplate.opsForValue()
                .get(CACHE_ROOM_KEY_PREFIX + roomUuid);
        return room == null;
    }



    public void addMoveList(String roomUuid, MoveDto move){
        redisMoveDtoTemplate.opsForList().rightPush(CACHE_MOVES_KEY_PREFIX + roomUuid, move);
        redisMoveDtoTemplate.expire(CACHE_MOVES_KEY_PREFIX + roomUuid, Duration.ofMinutes(24));
    }

    public void deleteRoom(Room room) {
        redisRoomTemplate.delete(CACHE_ROOM_KEY_PREFIX + room.getUuid());
    }

    public void deleteMoves(Room room) {
        redisMoveDtoTemplate.delete(CACHE_MOVES_KEY_PREFIX + room.getUuid());
    }

    public void clearMatchData(Room room){
        deleteUser(room.getFirstPlayer().getId());
        deleteUser(room.getSecondPlayer().getId());
        deleteRoom(room);
        deleteMoves(room);
    }
}
