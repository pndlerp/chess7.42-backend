package com.bebrample.backend.room;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoomResponseDto {
    private String uuid;
    private Long firstPlayerId;
    private Long secondPlayerId;
}
