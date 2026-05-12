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
@Table(name = "books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(unique = true)
    private String isbn;

    private String publisher;

    @Column(columnDefinition = "TEXT")
    private String description;

    public void update(String title, String author, String isbn, String publisher, String description) {
        if (title != null) this.title = title;
        if (author != null) this.author = author;
        if (isbn != null) this.isbn = isbn;
        if (publisher != null) this.publisher = publisher;
        if (description != null) this.description = description;
    }
}
