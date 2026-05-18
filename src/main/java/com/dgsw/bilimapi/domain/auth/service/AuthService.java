package com.dgsw.bilimapi.domain.auth.service;

import com.dgsw.bilimapi.commons.security.SecurityUtil;
import com.dgsw.bilimapi.commons.security.jwt.JwtProperties;
import com.dgsw.bilimapi.commons.security.jwt.JwtProvider;
import com.dgsw.bilimapi.domain.auth.dto.AccessTokenResponse;
import com.dgsw.bilimapi.domain.auth.dto.AuthResponse;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.domain.UserRole;
import com.dgsw.bilimapi.domain.auth.dto.LoginRequest;
import com.dgsw.bilimapi.domain.auth.dto.SignupRequest;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import com.dgsw.bilimapi.commons.security.CustomUserDetailsService;
import com.dgsw.bilimapi.domain.point.service.PointService;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;
    private final PointService pointService;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        UserEntity user = UserEntity.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(UserRole.USER)
                .lastSeenAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        pointService.initBalance(user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserEntity user = userDetailsService.loadEntityByEmail(authentication.getName());

        Duration accessExpiration = Duration.ofMinutes(jwtProperties.getAccessExpirationMinutes());
        Duration refreshExpiration = Duration.ofDays(jwtProperties.getRefreshExpirationDays());
        String accessToken = tokenProvider.generateToken(user, accessExpiration);
        String refreshToken = tokenProvider.generateToken(user, refreshExpiration);

        refreshTokenService.saveOrUpdate(user.getId(), refreshToken);
        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public void logout() {
        refreshTokenService.deleteByUserId(securityUtil.getCurrentUserId());
    }

    public AccessTokenResponse refresh(String refreshToken) {
        if (!tokenProvider.validToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."));
        String newAccessToken = tokenProvider.generateToken(user,
                Duration.ofMinutes(jwtProperties.getAccessExpirationMinutes()));
        return new AccessTokenResponse(newAccessToken);
    }
}
