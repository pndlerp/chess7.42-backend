package com.bebrample.backend.user;

import com.bebrample.backend.user.dto.UserCreateDto;
import com.bebrample.backend.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserRest {

    private final UserService userService;
    private final UserLoginDetailsService userLoginDetailsService;

    @PostMapping("/register")
    public UserResponseDto createAccount(@RequestBody UserCreateDto dto){
        return userService.save(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginToAccount(@RequestBody UserCreateDto dto){
        String token = userLoginDetailsService.login(dto);

        return ResponseEntity.ok(token);

    }
}
