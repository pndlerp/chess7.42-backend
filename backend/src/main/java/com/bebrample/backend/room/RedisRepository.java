package com.bebrample.backend.room;

import com.bebrample.backend.room.ws.dto.MoveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RedisRepository {
    private final RedisTemplate<String,Room> redisRoomTemplate;
    private final RedisTemplate<String,String> stringRedisTemplate;
    private final RedisTemplate<String, MoveDto> redisMoveDtoTemplate;

    private static final String CACHE_ROOM_KEY_PREFIX = "room:state:";
    private static final String CACHE_MOVES_KEY_PREFIX = "room:moves:";
    private static final String CACHE_LOBBY_PARTICIPANT_KEY_PREFIX = "user:";

    public void saveOrUpdateUser(String roomUuid, Long participantId){
        String key = CACHE_LOBBY_PARTICIPANT_KEY_PREFIX + participantId;
        Boolean exists = stringRedisTemplate.hasKey(key);
        stringRedisTemplate.opsForValue()
                .set(key, roomUuid, Duration.ofMinutes(24));
        if(Boolean.FALSE.equals(exists)){
            stringRedisTemplate.opsForSet().add("all_users", key);
        }
    }

    public String getUserRoomUuid(Long userId){
        return stringRedisTemplate.opsForValue()
                .get(CACHE_LOBBY_PARTICIPANT_KEY_PREFIX + userId);
    }

    public void deleteUser(Long userId){
        String key = CACHE_LOBBY_PARTICIPANT_KEY_PREFIX + userId;
        stringRedisTemplate.delete(key);
        stringRedisTemplate.opsForSet().remove("all_users", key);
    }
    public void deleteRoom(Room room) {
        String key = CACHE_ROOM_KEY_PREFIX + room.getUuid();
        redisRoomTemplate.delete(key);
        stringRedisTemplate.opsForSet().remove("all_rooms", key);
    }

    public void deleteMoves(Room room) {
        redisMoveDtoTemplate.delete(CACHE_MOVES_KEY_PREFIX + room.getUuid());
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

    public void saveOrUpdateRoom(Room room){
        String key = CACHE_ROOM_KEY_PREFIX + room.getUuid();
        Boolean exists = redisRoomTemplate.hasKey(key);
        redisRoomTemplate.opsForValue()
                .set(key, room, Duration.ofMinutes(24));

        if(Boolean.FALSE.equals(exists)) stringRedisTemplate.opsForSet().add("all_rooms", key);
    }

    public boolean isRoomNull(String roomUuid){
        Room room = redisRoomTemplate.opsForValue()
                .get(CACHE_ROOM_KEY_PREFIX + roomUuid);
        return room == null;
    }

    public List<Room> getAllRooms(){
        Set<String> keys = stringRedisTemplate.opsForSet().members("all_rooms");
        return redisRoomTemplate.opsForValue().multiGet(keys);
    }

    public List<String> getAllPlayingUsers(){
        Set<String> keys = stringRedisTemplate.opsForSet().members("all_users");
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }

    public void addMoveList(String roomUuid, MoveDto move){
        redisMoveDtoTemplate.opsForList().rightPush(CACHE_MOVES_KEY_PREFIX + roomUuid, move);
        redisMoveDtoTemplate.expire(CACHE_MOVES_KEY_PREFIX + roomUuid, Duration.ofMinutes(24));
    }



    public void clearMatchData(Room room){
        deleteUser(room.getFirstPlayer().getId());
        deleteUser(room.getSecondPlayer().getId());
        deleteRoom(room);
        deleteMoves(room);
    }
}
