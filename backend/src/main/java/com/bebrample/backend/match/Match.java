package com.bebrample.backend.match;

import com.bebrample.backend.entity.Result;
import com.bebrample.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "room_uuid")
    private String roomUuid;
    @ManyToOne
    @JoinColumn(name = "white_player_id")
    private User whitePlayer;
    @ManyToOne()
    @JoinColumn(name = "black_player_id")
    private User blackPlayer;
    @Column(name = "final_fen")
    private String finalFen;
    @Column(name = "pgn")
    private String pgn;
    @Column(name = "result")
    private Result result;


}
