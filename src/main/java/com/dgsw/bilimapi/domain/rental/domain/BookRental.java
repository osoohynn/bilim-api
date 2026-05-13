package com.dgsw.bilimapi.domain.rental.domain;

import com.dgsw.bilimapi.commons.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "book_rentals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class BookRental extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userBookId;

    @Column(nullable = false)
    private Long borrowerId;

    @Column(nullable = false)
    private Long lenderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalStatus status;

    private LocalDate dueDate;

    private LocalDateTime startedAt;

    private LocalDateTime returnedAt;

    public void accept(LocalDate dueDate) {
        this.status = RentalStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
        this.dueDate = dueDate;
    }

    public void reject() {
        this.status = RentalStatus.REJECTED;
    }

    public void returnBook() {
        this.status = RentalStatus.RETURNED;
        this.returnedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = RentalStatus.EXPIRED;
        this.returnedAt = LocalDateTime.now();
    }

    public void extendDueDate(LocalDate newDueDate) {
        this.dueDate = newDueDate;
    }
}
