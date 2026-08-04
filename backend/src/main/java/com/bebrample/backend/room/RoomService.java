package com.bebrample.backend.room;

import com.bebrample.backend.chess.ChessService;
import com.bebrample.backend.chess.MakeMoveResponseDto;
import com.bebrample.backend.common.exception.ResourcesNotFoundException;
import com.bebrample.backend.common.exception.RoomException;
import com.bebrample.backend.entity.Color;
import com.bebrample.backend.entity.RoomState;
import com.bebrample.backend.match.SaveMatchEvent;
import com.bebrample.backend.room.ws.dto.GameOverDto;
import com.bebrample.backend.room.ws.dto.MoveDto;
import com.bebrample.backend.room.ws.dto.WebSocketEvent;
import com.bebrample.backend.user.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {
    private final LobbyParticipantMapper lobbyParticipantMapper;
    private final RedisRepository redisRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ChessService chessService;

        public Room createRoom(){

            LobbyParticipant user = getUserFromContext();


            if(redisRepository.isPlaying(user.getId())) throw new RoomException("You already playing!");

            UserLobbyDto dto = lobbyParticipantMapper.toUserLobbyDto(user);
            Room room = new Room();
            room.setFirstPlayer(dto);
            room.setSecondPlayer(null);
            room.setRoomState(RoomState.WAITING_FOR_OPPONENT);

            redisRepository.updateUser(room.getUuid(), user.getId());
            redisRepository.updateRoom(room);
            updateRoom(room);
            return room;
        }
        public Room connectToRoom(String roomUuid){

            LobbyParticipant user = getUserFromContext();

            if(redisRepository.isPlaying(user.getId())) {
                throw new RoomException("User already playing other game");
            }
            Room room = redisRepository.getRoom(roomUuid);
            if (room == null) throw new RoomException("Room is null");
            if(room.getSecondPlayer() != null) throw new RoomException("Room is already full");
            UserLobbyDto dto = lobbyParticipantMapper.toUserLobbyDto(user);
            room.setSecondPlayer(dto);

            if(room.getSecondPlayer() != null) startGame(room);
            log.info("{} <- second player", room.getSecondPlayer());

            redisRepository.updateRoom(room);
            redisRepository.updateUser(room.getUuid(), user.getId());
            updateRoom(room);
            return room;
        }

        public void updateChessState(String roomUuid, MoveDto move, LobbyParticipant user){
            UserLobbyDto whoMadeMove = null;
            Room room = redisRepository.getRoom(roomUuid);
            if(room == null) throw new ResourcesNotFoundException("Room is null");
            Color atStart = room.getActiveColor();

            if(room.getRoomState() != RoomState.ONGOING) throw new RoomException("Game is finished. its over.");
            String redisRoomUuid = redisRepository.getUserRoomUuid(user.getId());
            if(redisRoomUuid == null) throw new ResourcesNotFoundException("You are not playing any game");
            if(!redisRoomUuid.equals(roomUuid)) throw new RoomException("You are not playing this game");

            if(user.getId().equals(room.getFirstPlayer().getId())) whoMadeMove = room.getFirstPlayer();
             else if(user.getId().equals(room.getSecondPlayer().getId())) whoMadeMove = room.getSecondPlayer();

             if(whoMadeMove == null) throw new ResourcesNotFoundException("how");
            if(whoMadeMove.getColor() != room.getActiveColor()) throw new RoomException("Not your move");

            String fen = room.getCurrentFen();
            MakeMoveResponseDto responseMove = chessService.makeMove(fen, move.getFrom() + move.getTo());
            switch (responseMove.getStatus()){
                case "checkmate": room.setRoomState(RoomState.CHECKMATE);
                case "stalemate": room.setRoomState(RoomState.STALEMATE);
                case "draw": room.setRoomState(RoomState.DRAW);
            }
            if(responseMove.getStatus().equals("checkmate") || responseMove.getStatus().equals("stalemate") || responseMove.getStatus().equals("draw")){
                endGame(roomUuid, whoMadeMove);
                return;
            }

            room.setCurrentFen(responseMove.getFen());
            simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomUuid, new WebSocketEvent<>("MOVE", responseMove));
            updateRoom(room);
            redisRepository.addMoveList(roomUuid, move);
//            if(responseMove.getIsCheckmate() || responseMove.getIsStalemate()){
//                endGame(roomUuid, whoMadeMove);
//                return;
//            }
            room.setActiveColor(atStart.toggle());
            redisRepository.updateRoom(room);
        }

        private void endGame(String roomUuid, UserLobbyDto whoMadeMove){

            Room room = redisRepository.getRoom(roomUuid);
            if(room == null){
                log.info("room is null");
                return;
            }
            redisRepository.updateRoom(room);
            if(!(room.getSecondPlayer() != null && (room.getSecondPlayer().getId() > 0 && room.getFirstPlayer().getId() > 0))) {
                log.info("game played with guest/not full lobby. prevent from saving");
                return;
            }
            List<MoveDto> moves = redisRepository.getMoves(room.getUuid());
            SaveMatchEvent event = new SaveMatchEvent(room, moves);
            eventPublisher.publishEvent(event);
            GameOverDto gameOverDto = new GameOverDto(whoMadeMove.getUsername(), room.getRoomState());
            updateRoom(room);
            redisRepository.clearMatchData(room);
            simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomUuid,
                    new WebSocketEvent<>("GAME_OVER", gameOverDto));
            log.info("saved game with room id:{}", roomUuid);
        }



        private void randomizeSides(Room room){
            boolean rng = ThreadLocalRandom.current().nextBoolean();
            if(rng){
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
            room.setCurrentFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            room.setActiveColor(Color.WHITE);
        }




        private LobbyParticipant getUserFromContext(){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return (LobbyParticipant) auth.getPrincipal();
        }

        private void updateRoom(Room room){
            simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getUuid(), new WebSocketEvent<>("ROOM_INFO", room ));
        }



}
