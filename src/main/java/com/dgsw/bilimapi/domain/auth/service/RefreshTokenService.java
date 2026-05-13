package com.dgsw.bilimapi.domain.auth.service;

import com.dgsw.bilimapi.domain.auth.domain.RefreshToken;
import com.dgsw.bilimapi.domain.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken saveOrUpdate(Long userId, String refreshToken) {
        return refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(refreshToken))
                .map(refreshTokenRepository::save)
                .orElseGet(() -> refreshTokenRepository.save(new RefreshToken(userId, refreshToken)));
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    }
}
