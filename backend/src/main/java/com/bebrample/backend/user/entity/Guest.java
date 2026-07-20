package com.bebrample.backend.user.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicLong;
@AllArgsConstructor
@NoArgsConstructor
public class Guest implements LobbyParticipant {
    private static final AtomicLong idGenerator = new AtomicLong(-1);
    private Long id = idGenerator.getAndDecrement();
    private String username = "bebra" + id;
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isGuest() {
        return true;
    }

    @Override
    public String getUsername() {
        return username + id;
    }
}
