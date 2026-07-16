package com.bebrample.backend.user;

import com.bebrample.backend.common.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class UserLoginDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found!"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public String login(User user){
        User user1 = userRepository.findUserById(user.getId());

        if(!passwordEncoder.matches(user.getPassword(), user1.getPassword())){
            throw new RuntimeException("wrong password");
        }
        return jwtService.generateToken(user1.getUsername());
    }
}
