package com.dgsw.bilimapi.domain.point.api;

import com.dgsw.bilimapi.domain.point.dto.ChargePointRequest;
import com.dgsw.bilimapi.domain.point.dto.PointResponse;
import com.dgsw.bilimapi.domain.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public PointResponse getBalance() {
        return pointService.getBalance();
    }

    @PostMapping("/charge")
    public PointResponse charge(@RequestBody @Valid ChargePointRequest request) {
        return pointService.charge(request.amount());
    }
}
