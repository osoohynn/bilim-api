package com.dgsw.bilimapi.domain.point.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_points")
@NoArgsConstructor
@Getter
public class UserPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private int balance;

    public UserPoint(Long userId) {
        this.userId = userId;
        this.balance = 0;
    }

    public void charge(int amount) {
        this.balance += amount;
    }

    public void deduct(int amount) {
        this.balance -= amount;
    }
}
