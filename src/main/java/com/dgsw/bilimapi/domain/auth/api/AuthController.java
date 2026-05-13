package com.dgsw.bilimapi.domain.auth.api;

import com.dgsw.bilimapi.domain.auth.dto.AccessTokenResponse;
import com.dgsw.bilimapi.domain.auth.dto.AuthResponse;
import com.dgsw.bilimapi.domain.auth.dto.TokenRefreshRequest;
import com.dgsw.bilimapi.domain.auth.service.AuthService;
import com.dgsw.bilimapi.domain.auth.dto.LoginRequest;
import com.dgsw.bilimapi.domain.auth.dto.SignupRequest;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@RequestBody @Valid SignupRequest request) {
        authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AccessTokenResponse refresh(@RequestBody @Valid TokenRefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        authService.logout(user.getId());
    }
}
