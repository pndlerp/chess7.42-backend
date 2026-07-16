package com.bebrample.backend.user;

import com.bebrample.backend.user.dto.UserCreateDto;
import com.bebrample.backend.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserLoginDetailsService userLoginDetailsService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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
}
