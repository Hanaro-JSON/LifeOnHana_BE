package com.example.lifeonhana.service;

import com.example.lifeonhana.dto.response.UserResponseDTO;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponseDTO getUserInfo(String authId) {
        User user = userRepository.findByAuthId(authId)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
            
        return new UserResponseDTO(
            user.getUserId(),
            user.getName(),
            user.getAuthId(),
            user.getIsFirst()
        );
    }
} 