package com.dgsw.bilimapi.domain.point.api;

import com.dgsw.bilimapi.domain.point.dto.ChargePointRequest;
import com.dgsw.bilimapi.domain.point.dto.PointResponse;
import com.dgsw.bilimapi.domain.point.service.PointService;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final UserRepository userRepository;

    @GetMapping
    public PointResponse getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        return pointService.getBalance(getCurrentUserId(userDetails));
    }

    @PostMapping("/charge")
    public PointResponse charge(@RequestBody @Valid ChargePointRequest request,
                                @AuthenticationPrincipal UserDetails userDetails) {
        return pointService.charge(getCurrentUserId(userDetails), request.amount());
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        UserEntity user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return user.getId();
    }
}
