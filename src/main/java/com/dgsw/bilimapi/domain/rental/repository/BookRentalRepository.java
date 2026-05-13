package com.dgsw.bilimapi.domain.rental.repository;

import com.dgsw.bilimapi.domain.rental.domain.BookRental;
import com.dgsw.bilimapi.domain.rental.domain.RentalStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRentalRepository extends JpaRepository<BookRental, Long> {

    List<BookRental> findByUserBookIdAndStatus(Long userBookId, RentalStatus status);

    @Query("SELECT r FROM BookRental r WHERE (r.borrowerId = :userId OR r.lenderId = :userId) AND r.status IN ('PENDING', 'ACTIVE')")
    List<BookRental> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM BookRental r WHERE r.status = 'ACTIVE' AND r.dueDate < :today")
    List<BookRental> findExpired(@Param("today") LocalDate today);
}
