package com.bebrample.backend.user;

import com.bebrample.backend.common.security.CookieService;
import com.bebrample.backend.user.dto.UserCreateDto;
import com.bebrample.backend.user.dto.UserResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserRest {

    private final UserService userService;
    private final CookieService cookieService;

    @PostMapping("/register")
    public UserResponseDto createAccount(@Valid @RequestBody UserCreateDto dto){
        return userService.save(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginToAccount(@Valid @RequestBody UserCreateDto dto, HttpServletResponse response){
        String jwt = userService.login(dto);
        ResponseCookie cookie = cookieService.createCookie(jwt);
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok("ok");

    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutFromAccount(HttpServletResponse response){
        ResponseCookie cookie = cookieService.deleteCookie();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/anonymous")
    public ResponseEntity<String> createAnonymousAccount(HttpServletResponse response){
        String jwt = userService.createGuest();
        ResponseCookie cookie = cookieService.createCookie(jwt);
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok("ok");
    }

}
