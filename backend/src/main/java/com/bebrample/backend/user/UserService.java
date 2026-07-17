package com.bebrample.backend.user;

import com.bebrample.backend.common.exception.ResourcesNotFoundException;
import com.bebrample.backend.common.security.JwtService;
import com.bebrample.backend.user.dto.UserCreateDto;
import com.bebrample.backend.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponseDto save(UserCreateDto dto){
//        User user = userMapper.toUser(dto);
//        userRepository.save(user);
//        return userMapper.toUserResponse(user);
        User user = new User();
        user.setUsername(dto.getUsername());
        String password = passwordEncoder.encode(dto.getPassword());
        user.setPassword(password);

        userRepository.save(user);
        return userMapper.toUserResponse(user);
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
