package com.dgsw.bilimapi.commons.security;

import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    public UserEntity getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
