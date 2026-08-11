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

        public Room createRoom(LobbyParticipant participant){



            if(redisRepository.isPlaying(participant.getId())) throw new RoomException("You already playing!");

            UserLobbyDto dto = lobbyParticipantMapper.toUserLobbyDto(participant);
            Room room = new Room();
            room.setFirstPlayer(dto);
            room.setSecondPlayer(null);
            room.setRoomState(RoomState.WAITING_FOR_OPPONENT);

            redisRepository.updateUser(room.getUuid(), participant.getId());
            redisRepository.updateRoom(room);
            sendUpdateMessage(room);
            return room;
        }
        public Room connectToRoom(String roomUuid, LobbyParticipant user){

            if(redisRepository.isPlaying(user.getId())) {
                throw new RoomException("User already playing other game");
            }
            Room room = redisRepository.getRoom(roomUuid);
            if (room == null) throw new RoomException("Room is null");
            if(room.getSecondPlayer() != null) throw new RoomException("Room is already full");
            UserLobbyDto dto = lobbyParticipantMapper.toUserLobbyDto(user);
            room.setSecondPlayer(dto);
            startGame(room);
            log.info("{} <- second player", room.getSecondPlayer());

            redisRepository.updateRoom(room);
            redisRepository.updateUser(room.getUuid(), user.getId());
            sendUpdateMessage(room);
            return room;
        }

        public void updateChessState(String roomUuid, MoveDto move, LobbyParticipant user){
            Room room = redisRepository.getRoom(roomUuid);
            if(room == null) throw new ResourcesNotFoundException("Room is null");
            if(room.getRoomState() != RoomState.ONGOING && room.getRoomState() != RoomState.CHECK) throw new RoomException("Game is finished. its over.");

            UserLobbyDto whoMadeMove = null;
            Color atStart = room.getActiveColor();

            String redisRoomUuid = redisRepository.getUserRoomUuid(user.getId());
            if(!roomUuid.equals(redisRoomUuid)) throw new RoomException("You are not playing this game");

            if(user.getId().equals(room.getFirstPlayer().getId())) whoMadeMove = room.getFirstPlayer();
             else if(user.getId().equals(room.getSecondPlayer().getId())) whoMadeMove = room.getSecondPlayer();

            if(whoMadeMove == null) throw new ResourcesNotFoundException("Player somehow not authorized");
            if(whoMadeMove.getColor() != room.getActiveColor()) throw new RoomException("Not your move");

            String fen = room.getCurrentFen();
            MakeMoveResponseDto responseMove = chessService.makeMove(fen, move.getFrom() + move.getTo());
            if(responseMove.getGameStatus() != null) {
                switch (responseMove.getGameStatus()) {
                    case "checkmate":
                        room.setRoomState(RoomState.CHECKMATE);
                    case "stalemate":
                        room.setRoomState(RoomState.STALEMATE);
                    case "draw":
                        room.setRoomState(RoomState.DRAW);
                        break;
                }
                if(responseMove.getGameStatus().equals("checkmate") || responseMove.getGameStatus().equals("stalemate") || responseMove.getGameStatus().equals("draw")) {
                    endGame(roomUuid, whoMadeMove);
                    return;
                }
            }

            room.setCurrentFen(responseMove.getFen());
            simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomUuid, new WebSocketEvent<>("MOVE", responseMove));
            sendUpdateMessage(room);
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
                //TODO: refactor db to add possibility to save games with guests
                return;
            }
            List<MoveDto> moves = redisRepository.getMoves(room.getUuid());
            SaveMatchEvent event = new SaveMatchEvent(room, moves);
            eventPublisher.publishEvent(event);
            GameOverDto gameOverDto = new GameOverDto(whoMadeMove.getUsername(), room.getRoomState());
            sendUpdateMessage(room);
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

        private void sendUpdateMessage(Room room){
            simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getUuid(), new WebSocketEvent<>("ROOM_INFO", room ));
        }



}
