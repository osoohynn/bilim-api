package com.dgsw.bilimapi.domain.point.repository;

import com.dgsw.bilimapi.domain.point.domain.UserPoint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

    Optional<UserPoint> findByUserId(Long userId);
}
