package com.dgsw.bilimapi.domain.rental;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.bilimapi.BaseControllerTest;
import com.dgsw.bilimapi.JwtFactory;
import com.dgsw.bilimapi.commons.security.jwt.JwtProperties;
import com.dgsw.bilimapi.domain.book.domain.Book;
import com.dgsw.bilimapi.domain.book.domain.UserBook;
import com.dgsw.bilimapi.domain.book.repository.BookRepository;
import com.dgsw.bilimapi.domain.book.repository.UserBookRepository;
import com.dgsw.bilimapi.domain.friend.domain.Friendship;
import com.dgsw.bilimapi.domain.friend.domain.FriendshipStatus;
import com.dgsw.bilimapi.domain.friend.repository.FriendshipRepository;
import com.dgsw.bilimapi.domain.rental.domain.BookRental;
import com.dgsw.bilimapi.domain.rental.domain.RentalStatus;
import com.dgsw.bilimapi.domain.rental.dto.AcceptRentalRequest;
import com.dgsw.bilimapi.domain.rental.dto.LendRequest;
import com.dgsw.bilimapi.domain.rental.repository.BookRentalRepository;
import com.dgsw.bilimapi.domain.rental.repository.RentalExtensionRepository;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.domain.UserRole;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class RentalControllerTest extends BaseControllerTest {

    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private UserBookRepository userBookRepository;
    @Autowired private BookRentalRepository rentalRepository;
    @Autowired private RentalExtensionRepository extensionRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private JwtProperties jwtProperties;

    private UserEntity owner;
    private UserEntity borrower;
    private String ownerToken;
    private String borrowerToken;
    private Book book;
    private UserBook userBook;

    @BeforeEach
    void setUp() {
        extensionRepository.deleteAll();
        rentalRepository.deleteAll();
        friendshipRepository.deleteAll();
        userBookRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        owner = saveUser("owner@bilim.com");
        borrower = saveUser("borrower@bilim.com");
        ownerToken = createToken(owner);
        borrowerToken = createToken(borrower);

        friendshipRepository.save(Friendship.builder()
                .requesterId(owner.getId()).recipientId(borrower.getId())
                .status(FriendshipStatus.ACCEPTED).build());

        book = bookRepository.save(Book.builder()
                .title("클린 코드").author("로버트 마틴").price(0).build());

        userBook = userBookRepository.save(UserBook.builder()
                .bookId(book.getId()).ownerId(owner.getId()).holderId(owner.getId()).isPublic(true).build());
    }

    @DisplayName("requestRental: 대여 요청에 성공하면 201을 반환한다.")
    @Test
    void requestRental_success() throws Exception {
        mockMvc.perform(post("/api/rentals/request/" + userBook.getId())
                        .header("Authorization", "Bearer " + borrowerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @DisplayName("requestRental: 비공개 책은 대여 요청 불가 (403).")
    @Test
    void requestRental_notPublic() throws Exception {
        userBook.setPublic(false);
        userBookRepository.save(userBook);

        mockMvc.perform(post("/api/rentals/request/" + userBook.getId())
                        .header("Authorization", "Bearer " + borrowerToken))
                .andExpect(status().isForbidden());
    }

    @DisplayName("accept: 대여 수락에 성공하면 200을 반환한다.")
    @Test
    void accept_success() throws Exception {
        BookRental rental = savePendingRental();

        String body = objectMapper.writeValueAsString(new AcceptRentalRequest(7));

        mockMvc.perform(post("/api/rentals/" + rental.getId() + "/accept")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @DisplayName("reject: 대여 거절에 성공하면 200을 반환한다.")
    @Test
    void reject_success() throws Exception {
        BookRental rental = savePendingRental();

        mockMvc.perform(post("/api/rentals/" + rental.getId() + "/reject")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());
    }

    @DisplayName("lend: 빌려주기에 성공하면 201을 반환한다.")
    @Test
    void lend_success() throws Exception {
        String body = objectMapper.writeValueAsString(
                new LendRequest(borrower.getId(), userBook.getId(), 7));

        mockMvc.perform(post("/api/rentals/lend")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @DisplayName("returnBook: 반납에 성공하면 200을 반환한다.")
    @Test
    void returnBook_success() throws Exception {
        BookRental rental = saveActiveRental();

        mockMvc.perform(post("/api/rentals/" + rental.getId() + "/return")
                        .header("Authorization", "Bearer " + borrowerToken))
                .andExpect(status().isOk());
    }

    @DisplayName("getMyRentals: 내 대여 목록을 반환한다.")
    @Test
    void getMyRentals() throws Exception {
        savePendingRental();

        mockMvc.perform(get("/api/rentals")
                        .header("Authorization", "Bearer " + borrowerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    private BookRental savePendingRental() {
        return rentalRepository.save(BookRental.builder()
                .userBookId(userBook.getId())
                .borrowerId(borrower.getId())
                .lenderId(owner.getId())
                .status(RentalStatus.PENDING)
                .build());
    }

    private BookRental saveActiveRental() {
        userBook.transferTo(borrower.getId());
        userBookRepository.save(userBook);
        return rentalRepository.save(BookRental.builder()
                .userBookId(userBook.getId())
                .borrowerId(borrower.getId())
                .lenderId(owner.getId())
                .status(RentalStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .dueDate(LocalDate.now().plusDays(7))
                .build());
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
