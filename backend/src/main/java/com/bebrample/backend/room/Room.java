package com.bebrample.backend.room;

import com.bebrample.backend.entity.GameCondition;
import com.bebrample.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid = UUID.randomUUID().toString();
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_player_id")
    private User firstPlayer;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_player_id")
    private User secondPlayer;

    private GameCondition status;
}
