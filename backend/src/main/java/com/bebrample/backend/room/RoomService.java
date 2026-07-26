package com.bebrample.backend.room;

import com.bebrample.backend.common.exception.ResourcesNotFoundException;
import com.bebrample.backend.common.exception.RoomException;
import com.bebrample.backend.entity.Color;
import com.bebrample.backend.entity.RoomState;
import com.bebrample.backend.match.Match;
import com.bebrample.backend.match.MatchRepository;
import com.bebrample.backend.room.ws.dto.MoveDto;
import com.bebrample.backend.room.ws.dto.WebSocketEvent;
import com.bebrample.backend.user.UserRepository;
import com.bebrample.backend.user.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {
    private final LobbyParticipantMapper lobbyParticipantMapper;
    private final RedisTemplate<String,Room> redisRoomTemplate;
    private final RedisTemplate<String,String> stringRedisTemplate;
    private final RedisTemplate<String, MoveDto> redisMoveDtoTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private boolean moveChanger;

    private static final String CACHE_ROOM_KEY_PREFIX = "room:state:";
    private static final String CACHE_MOVES_KEY_PREFIX = "room:moves:";
    private static final String CACHE_LOBBY_PARTICIPANT_KEY_PREFIX = "user:";

        public Room createRoom(){

            LobbyParticipant user = getUserFromContext();

            String player = stringRedisTemplate.opsForValue()
                    .get(CACHE_LOBBY_PARTICIPANT_KEY_PREFIX + user.getId());
            if(player != null) throw new RoomException("You already playing!");

            UserLobbyDto dto = lobbyParticipantMapper.toUserLobbyDto(user);
            Room room = new Room();
            room.setFirstPlayer(dto);
            room.setSecondPlayer(null);
            room.setRoomState(RoomState.WAITING_FOR_OPPONENT);

            stringRedisTemplate.opsForValue()
                    .set(CACHE_LOBBY_PARTICIPANT_KEY_PREFIX + user.getId(),
                            room.getUuid(), Duration.ofMinutes(30));
            redisRoomTemplate.opsForValue()
                    .set(CACHE_ROOM_KEY_PREFIX + room.getUuid(),
                            room, Duration.ofMinutes(30));
            return room;
        }
        public Room connectToRoom(String roomUuid){

            LobbyParticipant user = getUserFromContext();


            String player = stringRedisTemplate.opsForValue()
                    .get(CACHE_LOBBY_PARTICIPANT_KEY_PREFIX + user.getId());
            if (player != null) throw new RoomException("You already playing!");
            Room room = redisRoomTemplate.opsForValue()
                    .get(CACHE_ROOM_KEY_PREFIX + roomUuid);

            UserLobbyDto dto = lobbyParticipantMapper.toUserLobbyDto(user);
            room.setSecondPlayer(dto);

            if(room.getSecondPlayer() != null) startGame(room);
            log.info("{} <- second player", room.getSecondPlayer());

            redisRoomTemplate.opsForValue()
                    .set(CACHE_ROOM_KEY_PREFIX + room.getUuid(),
                            room, Duration.ofMinutes(30));
            stringRedisTemplate.opsForValue()
                    .set(CACHE_LOBBY_PARTICIPANT_KEY_PREFIX + user.getId(),
                            room.getUuid(), Duration.ofMinutes(30));
            return room;
        }

        public void updateChessState(String roomUuid, MoveDto move, LobbyParticipant user){
            UserLobbyDto whoMadeMove = null;
            String key = CACHE_MOVES_KEY_PREFIX + roomUuid;
            Room room = redisRoomTemplate.opsForValue().get("room:state:" + roomUuid);
            Color atStart = room.getActiveColor();

            if(room.getRoomState() == RoomState.FINISHED) throw new RoomException("Game is finished. its over.");
            String redisRoomUuid = stringRedisTemplate.opsForValue().get("user:" + user.getId());
            if(!redisRoomUuid.equals(roomUuid)) throw new RoomException("you aint even playin this game bradar");

            if(user.getId().equals(room.getFirstPlayer().getId())) whoMadeMove = room.getFirstPlayer();
             else if(user.getId().equals(room.getSecondPlayer().getId())) whoMadeMove = room.getSecondPlayer();
            if(whoMadeMove.getColor() != room.getActiveColor()) throw new RoomException("not your move");
            redisMoveDtoTemplate.opsForList().rightPush(key, move);
            redisMoveDtoTemplate.expire(key, Duration.ofMinutes(30));
            simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomUuid, new WebSocketEvent<>("MOVE", move));
            room.setActiveColor(atStart.toggle());
            redisRoomTemplate.opsForValue().set("room:state:" + roomUuid, room);
            if (move.getTo().equals("ee") && move.getFrom().equals("ee")) endGame(roomUuid);

        }

        private void endGame(String roomUuid){
            String movesKey = CACHE_MOVES_KEY_PREFIX + roomUuid;
            Room room = redisRoomTemplate.opsForValue()
                    .get(CACHE_ROOM_KEY_PREFIX + roomUuid);
            if(room == null){
                log.info("room is null");
                return;
            }
            room.setRoomState(RoomState.FINISHED);
            redisRoomTemplate.opsForValue().set("room:state:" + room.getUuid(), room);
            List<MoveDto> moves = redisMoveDtoTemplate.opsForList().range(movesKey, 0, -1);

            if(!(room.getSecondPlayer() != null && (room.getSecondPlayer().getId() > 0 && room.getFirstPlayer().getId() > 0))) {
                log.info("game played with guest/not full lobby. prevent from saving");
                return;
            }
            Match match = new Match();
            match.setRoomUuid(room.getUuid());
            User user1 = userRepository.findUserById(room.getFirstPlayer().getId())
                    .orElseThrow(() -> new ResourcesNotFoundException("User not found in db"));
            User user2 = userRepository.findUserById(room.getSecondPlayer().getId())
                    .orElseThrow(() -> new ResourcesNotFoundException("User not found in db"));
            match.setBlackPlayer(user1);
            match.setWhitePlayer(user2);
            match.setFinalFen(null);
            match.setResult(null);
            match.setPgn(generatePgn(moves));

            matchRepository.save(match);
            log.info("saved game with room id:{}", roomUuid);

        }

        private String generatePgn(List<MoveDto> moves){
            return moves.stream()
                    .map(move -> move.getFrom() + "-" + move.getTo())
                    .collect(Collectors.joining(" "));
        }

        private void randomizeSides(Room room){
            boolean RNGGOD = ThreadLocalRandom.current().nextBoolean();
            if(RNGGOD){
                // if rng god wants to leave it as it is, then it should be like that.
                room.getFirstPlayer().setColor(Color.WHITE);
                room.getSecondPlayer().setColor(Color.BLACK);
            } else {
                room.getFirstPlayer().setColor(Color.BLACK);
                room.getSecondPlayer().setColor(Color.WHITE);
            }
        }

        private void startGame(Room room){
            randomizeSides(room);
            room.setRoomState(RoomState.ONGOING);
            room.setActiveColor(Color.WHITE);
        }




        private LobbyParticipant getUserFromContext(){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return (LobbyParticipant) auth.getPrincipal();
        }



}
