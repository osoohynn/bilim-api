package com.dgsw.bilimapi.domain.point.service;

import com.dgsw.bilimapi.commons.security.SecurityUtil;
import com.dgsw.bilimapi.domain.point.domain.UserPoint;
import com.dgsw.bilimapi.domain.point.dto.PointResponse;
import com.dgsw.bilimapi.domain.point.exception.InsufficientPointsException;
import com.dgsw.bilimapi.domain.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointRepository userPointRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public PointResponse getBalance() {
        UserPoint userPoint = getOrCreate(securityUtil.getCurrentUserId());
        return new PointResponse(userPoint.getBalance());
    }

    @Transactional
    public PointResponse charge(int amount) {
        UserPoint userPoint = getOrCreate(securityUtil.getCurrentUserId());
        userPoint.charge(amount);
        return new PointResponse(userPoint.getBalance());
    }

    @Transactional
    public void deduct(Long userId, int amount) {
        UserPoint userPoint = getOrCreate(userId);
        if (userPoint.getBalance() < amount) {
            throw new InsufficientPointsException();
        }
        userPoint.deduct(amount);
    }

    private UserPoint getOrCreate(Long userId) {
        return userPointRepository.findByUserId(userId)
                .orElseGet(() -> userPointRepository.save(new UserPoint(userId)));
    }
}
