package com.dgsw.bilimapi.domain.rental.dto;

import com.dgsw.bilimapi.domain.rental.domain.BookRental;
import com.dgsw.bilimapi.domain.rental.domain.RentalStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RentalResponse(
        Long id,
        Long userBookId,
        Long borrowerId,
        Long lenderId,
        RentalStatus status,
        LocalDate dueDate,
        LocalDateTime startedAt,
        LocalDateTime createdAt
) {
    public static RentalResponse from(BookRental rental) {
        return new RentalResponse(
                rental.getId(),
                rental.getUserBookId(),
                rental.getBorrowerId(),
                rental.getLenderId(),
                rental.getStatus(),
                rental.getDueDate(),
                rental.getStartedAt(),
                rental.getCreatedAt()
        );
    }
}
