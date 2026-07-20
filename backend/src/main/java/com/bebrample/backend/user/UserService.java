package com.bebrample.backend.user;

import com.bebrample.backend.common.exception.ResourcesNotFoundException;
import com.bebrample.backend.common.security.JwtService;
import com.bebrample.backend.user.dto.UserCreateDto;
import com.bebrample.backend.user.dto.UserResponseDto;
import com.bebrample.backend.user.entity.Guest;
import com.bebrample.backend.user.entity.User;
import jakarta.transaction.Transactional;
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
    @Transactional
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
    @Transactional
    public String login(UserCreateDto dto){
        User user = userRepository.findUserByUsername(dto.getUsername())
                .orElseThrow(() -> new ResourcesNotFoundException("User not found"));

        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())){
            throw new ResourcesNotFoundException("Wrong Password");
        }
        return jwtService.generateToken(user.getId().toString(),user.getUsername(), "ROLE_USER");
    }

    public String createGuest(){
        Guest guest = new Guest();
        return jwtService.generateToken(guest.getId().toString(),guest.getUsername(), "ROLE_GUEST");
    }


}
