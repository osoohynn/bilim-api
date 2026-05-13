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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "rental_extensions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class RentalExtension extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long rentalId;

    @Column(nullable = false)
    private LocalDate requestedDueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExtensionStatus status;

    public void accept() {
        this.status = ExtensionStatus.ACCEPTED;
    }

    public void reject() {
        this.status = ExtensionStatus.REJECTED;
    }
}
