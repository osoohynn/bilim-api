package com.dgsw.bilimapi.domain.book.domain;

import com.dgsw.bilimapi.commons.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class UserBook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false, name = "owner_id")
    private Long ownerId;

    @Column(nullable = false, name = "holder_id")
    private Long holderId;

    @Column(nullable = false)
    private boolean isPublic;

    public void transferTo(Long newHolderId) {
        this.holderId = newHolderId;
    }

    public void returnToOwner() {
        this.holderId = this.ownerId;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isRentedOut() {
        return !ownerId.equals(holderId);
    }
}
