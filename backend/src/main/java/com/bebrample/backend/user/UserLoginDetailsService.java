package com.bebrample.backend.user;

import com.bebrample.backend.common.exception.ResourcesNotFoundException;
import com.bebrample.backend.common.security.JwtService;
import com.bebrample.backend.user.dto.UserCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLoginDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found!"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public String login(UserCreateDto user){
        User user1 = userRepository.findUserByUsername(user.getUsername())
                .orElseThrow(() -> new ResourcesNotFoundException("User not found"));

        if(!passwordEncoder.matches(user.getPassword(), user1.getPassword())){
            throw new ResourcesNotFoundException("Wrong Password");
        }
        return jwtService.generateToken(user1.getUsername());
    }
}
