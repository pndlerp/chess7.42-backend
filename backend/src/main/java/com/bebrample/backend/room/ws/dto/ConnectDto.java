package com.bebrample.backend.room.ws.dto;

import com.bebrample.backend.entity.Role;
import com.bebrample.backend.entity.RoomState;

public class ConnectDto {
    Role role;
    String playerName;
    String opponentName;
    RoomState roomState;
}
