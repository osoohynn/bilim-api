package com.dgsw.bilimapi.domain.rental.repository;

import com.dgsw.bilimapi.domain.rental.domain.ExtensionStatus;
import com.dgsw.bilimapi.domain.rental.domain.RentalExtension;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalExtensionRepository extends JpaRepository<RentalExtension, Long> {

    Optional<RentalExtension> findByRentalIdAndStatus(Long rentalId, ExtensionStatus status);
}
