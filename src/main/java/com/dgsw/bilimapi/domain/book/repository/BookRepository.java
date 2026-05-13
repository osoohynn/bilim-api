package com.dgsw.bilimapi.domain.book.repository;

import com.dgsw.bilimapi.domain.book.domain.Book;
import com.dgsw.bilimapi.domain.book.domain.BookCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);

    @Query("SELECT b FROM Book b WHERE " +
            "(:keyword IS NULL OR b.title LIKE %:keyword% OR b.author LIKE %:keyword%) AND " +
            "(:category IS NULL OR b.category = :category)")
    List<Book> search(@Param("keyword") String keyword, @Param("category") BookCategory category);
}
