package com.dgsw.bilimapi.domain.bookshelf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.bilimapi.BaseControllerTest;
import com.dgsw.bilimapi.JwtFactory;
import com.dgsw.bilimapi.commons.security.jwt.JwtProperties;
import com.dgsw.bilimapi.domain.book.domain.Book;
import com.dgsw.bilimapi.domain.book.domain.UserBook;
import com.dgsw.bilimapi.domain.book.repository.BookRepository;
import com.dgsw.bilimapi.domain.book.repository.BookWishlistRepository;
import com.dgsw.bilimapi.domain.book.repository.UserBookRepository;
import com.dgsw.bilimapi.domain.bookshelf.dto.VisibilityRequest;
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

class BookshelfControllerTest extends BaseControllerTest {

    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private UserBookRepository userBookRepository;
    @Autowired private BookWishlistRepository bookWishlistRepository;
    @Autowired private JwtProperties jwtProperties;

    private UserEntity user;
    private String token;
    private Book book;

    @BeforeEach
    void setUp() {
        bookWishlistRepository.deleteAll();
        userBookRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        user = saveUser("user@bilim.com");
        token = createToken(user);
        book = bookRepository.save(Book.builder()
                .title("클린 코드").author("로버트 마틴").price(1000).build());
    }

    @DisplayName("getMyBookshelf: 내 책장을 반환한다.")
    @Test
    void getMyBookshelf() throws Exception {
        userBookRepository.save(UserBook.builder()
                .bookId(book.getId()).ownerId(user.getId()).holderId(user.getId()).isPublic(false).build());

        mockMvc.perform(get("/api/bookshelf")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owned[0].title").value("클린 코드"));
    }

    @DisplayName("setVisibility: 책 공개 여부를 변경한다.")
    @Test
    void setVisibility() throws Exception {
        UserBook userBook = userBookRepository.save(UserBook.builder()
                .bookId(book.getId()).ownerId(user.getId()).holderId(user.getId()).isPublic(false).build());

        String body = objectMapper.writeValueAsString(new VisibilityRequest(true));

        mockMvc.perform(patch("/api/bookshelf/" + userBook.getId() + "/visibility")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @DisplayName("getFriendBookshelf: 친구의 공개 책장을 반환한다.")
    @Test
    void getFriendBookshelf() throws Exception {
        UserEntity friend = saveUser("friend@bilim.com");
        userBookRepository.save(UserBook.builder()
                .bookId(book.getId()).ownerId(friend.getId()).holderId(friend.getId()).isPublic(true).build());

        mockMvc.perform(get("/api/bookshelf/" + friend.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("클린 코드"));
    }

    @DisplayName("addWishlist: 찜 추가에 성공하면 201을 반환한다.")
    @Test
    void addWishlist() throws Exception {
        mockMvc.perform(post("/api/bookshelf/wishlist/" + book.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
    }

    @DisplayName("addWishlist: 중복 찜 시 409를 반환한다.")
    @Test
    void addWishlist_duplicate() throws Exception {
        mockMvc.perform(post("/api/bookshelf/wishlist/" + book.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/bookshelf/wishlist/" + book.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @DisplayName("removeWishlist: 찜 삭제에 성공하면 204를 반환한다.")
    @Test
    void removeWishlist() throws Exception {
        mockMvc.perform(post("/api/bookshelf/wishlist/" + book.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/bookshelf/wishlist/" + book.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    private UserEntity saveUser(String email) {
        return userRepository.save(UserEntity.builder()
                .email(email).password("encoded").nickname("유저")
                .role(UserRole.USER).lastSeenAt(LocalDateTime.now()).build());
    }

    private String createToken(UserEntity user) {
        return JwtFactory.builder()
                .subject(user.getEmail())
                .claims(Map.of("id", user.getId()))
                .expiration(new Date(System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()))
                .build().createToken(jwtProperties);
    }
}
