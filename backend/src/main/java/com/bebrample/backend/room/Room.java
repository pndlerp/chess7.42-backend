package com.bebrample.backend.room;

import com.bebrample.backend.entity.GameCondition;
import com.bebrample.backend.user.User;
import jakarta.persistence.*;
import lombok.*;

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
    @OneToOne
    @JoinColumn(name = "first_player_id")
    private User firstPlayer;
    @OneToOne
    @JoinColumn(name = "second_player_id")
    private User secondPlayer;

    private GameCondition status;
}
