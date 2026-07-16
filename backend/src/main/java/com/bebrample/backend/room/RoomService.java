package com.bebrample.backend.room;

import com.bebrample.backend.common.exception.ResourcesNotFoundException;
import com.bebrample.backend.common.exception.RoomAlreadyFullException;
import com.bebrample.backend.user.User;
import com.bebrample.backend.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
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

        if(room.getFirstPlayer().equals(user)) throw new RoomAlreadyFullException("cant be same player");
        if(room.getSecondPlayer() != null) throw new RoomAlreadyFullException("room already full");
        room.setSecondPlayer(user);
        return roomRepository.save(room);
    }
}
