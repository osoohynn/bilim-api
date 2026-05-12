package com.dgsw.bilimapi.domain.book;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.bilimapi.BaseControllerTest;
import com.dgsw.bilimapi.JwtFactory;
import com.dgsw.bilimapi.commons.security.jwt.JwtProperties;
import com.dgsw.bilimapi.domain.book.domain.Book;
import com.dgsw.bilimapi.domain.book.dto.CreateBookRequest;
import com.dgsw.bilimapi.domain.book.dto.UpdateBookRequest;
import com.dgsw.bilimapi.domain.book.repository.BookRepository;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.domain.UserRole;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class BookControllerTest extends BaseControllerTest {

    @Autowired private BookRepository bookRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtProperties jwtProperties;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        userRepository.deleteAll();

        UserEntity admin = saveUser("admin@bilim.com", UserRole.ADMIN);
        UserEntity user = saveUser("user@bilim.com", UserRole.USER);
        adminToken = createToken(admin);
        userToken = createToken(user);
    }

    @DisplayName("create: ADMIN이 책을 등록하면 201을 반환한다.")
    @Test
    void create_success() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateBookRequest("클린 코드", "로버트 마틴", "9788966260959", "인사이트", "좋은 책"));

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("클린 코드"))
                .andExpect(jsonPath("$.author").value("로버트 마틴"));
    }

    @DisplayName("create: 중복 ISBN 등록 시 409를 반환한다.")
    @Test
    void create_duplicateIsbn() throws Exception {
        saveBook("클린 코드", "로버트 마틴", "9788966260959");

        String body = objectMapper.writeValueAsString(
                new CreateBookRequest("다른 책", "다른 저자", "9788966260959", null, null));

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @DisplayName("create: USER 권한으로 등록 시 403을 반환한다.")
    @Test
    void create_forbidden() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateBookRequest("클린 코드", "로버트 마틴", null, null, null));

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @DisplayName("findAll: 책 목록을 반환한다.")
    @Test
    void findAll_success() throws Exception {
        saveBook("클린 코드", "로버트 마틴", "9788966260959");
        saveBook("리팩터링", "마틴 파울러", "9791162242742");

        mockMvc.perform(get("/api/books")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @DisplayName("findById: 책 단건 조회에 성공한다.")
    @Test
    void findById_success() throws Exception {
        Book book = saveBook("클린 코드", "로버트 마틴", "9788966260959");

        mockMvc.perform(get("/api/books/" + book.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("클린 코드"));
    }

    @DisplayName("findById: 없는 책 조회 시 404를 반환한다.")
    @Test
    void findById_notFound() throws Exception {
        mockMvc.perform(get("/api/books/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @DisplayName("update: ADMIN이 책을 수정하면 200을 반환한다.")
    @Test
    void update_success() throws Exception {
        Book book = saveBook("클린 코드", "로버트 마틴", "9788966260959");

        String body = objectMapper.writeValueAsString(
                new UpdateBookRequest("클린 코드 2판", null, null, null, null));

        mockMvc.perform(put("/api/books/" + book.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("클린 코드 2판"))
                .andExpect(jsonPath("$.author").value("로버트 마틴"));
    }

    @DisplayName("update: 없는 책 수정 시 404를 반환한다.")
    @Test
    void update_notFound() throws Exception {
        String body = objectMapper.writeValueAsString(
                new UpdateBookRequest("제목", null, null, null, null));

        mockMvc.perform(put("/api/books/999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @DisplayName("delete: ADMIN이 책을 삭제하면 204를 반환한다.")
    @Test
    void delete_success() throws Exception {
        Book book = saveBook("클린 코드", "로버트 마틴", null);

        mockMvc.perform(delete("/api/books/" + book.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @DisplayName("delete: 없는 책 삭제 시 404를 반환한다.")
    @Test
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/api/books/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    private UserEntity saveUser(String email, UserRole role) {
        return userRepository.save(UserEntity.builder()
                .email(email)
                .password("encoded")
                .nickname("테스트유저")
                .role(role)
                .lastSeenAt(LocalDateTime.now())
                .build());
    }

    private Book saveBook(String title, String author, String isbn) {
        return bookRepository.save(Book.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .build());
    }

    private String createToken(UserEntity user) {
        return JwtFactory.builder()
                .subject(user.getEmail())
                .claims(Map.of("id", user.getId()))
                .expiration(new Date(System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()))
                .build()
                .createToken(jwtProperties);
    }
}
