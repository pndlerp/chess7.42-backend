package com.bebrample.backend.room;

import com.bebrample.backend.common.exception.ResourcesNotFoundException;
import com.bebrample.backend.common.exception.RoomException;
import com.bebrample.backend.user.User;
import com.bebrample.backend.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private final SimpMessagingTemplate simpMessagingTemplate;
    @Transactional
    public Long createRoom(){
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findUserByUsername(currentUsername).orElseThrow(() ->
                    new ResourcesNotFoundException("i dunno how this could happened"));

            Room room = new Room();
            room.setFirstPlayer(user);
            room.setSecondPlayer(null);
            room.setStatus(null);

            roomRepository.save(room);
            return room.getId();
    }
    @Transactional
    public Room connectToRoom(Long roomId){
        Room room = roomRepository.findById(roomId).orElseThrow(() ->
                new ResourcesNotFoundException("Room with id " + roomId + "not found"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByUsername(currentUsername).orElseThrow(() ->
                new ResourcesNotFoundException("i dunno how this could happened"));

        if(room.getFirstPlayer().equals(user)) throw new RoomException("cant be same player");
        if(room.getSecondPlayer() != null) throw new RoomException("room already full");
        room.setSecondPlayer(user);

        Room savedRoom = roomRepository.save(room);

        simpMessagingTemplate.convertAndSend("/topic/room/1" + roomId, savedRoom);

        return savedRoom;
    }

    public void sendMessage(){
        simpMessagingTemplate.convertAndSend("topic/room/1", "server here");
    }

}
