package com.dgsw.bilimapi.domain.rental.service;

import com.dgsw.bilimapi.domain.book.domain.UserBook;
import com.dgsw.bilimapi.domain.book.exception.BookNotFoundException;
import com.dgsw.bilimapi.domain.book.repository.UserBookRepository;
import com.dgsw.bilimapi.domain.friend.repository.FriendshipRepository;
import com.dgsw.bilimapi.domain.rental.domain.BookRental;
import com.dgsw.bilimapi.domain.rental.domain.ExtensionStatus;
import com.dgsw.bilimapi.domain.rental.domain.RentalExtension;
import com.dgsw.bilimapi.domain.rental.domain.RentalStatus;
import com.dgsw.bilimapi.domain.rental.dto.AcceptRentalRequest;
import com.dgsw.bilimapi.domain.rental.dto.ExtendRequest;
import com.dgsw.bilimapi.domain.rental.dto.LendRequest;
import com.dgsw.bilimapi.domain.rental.dto.RentalResponse;
import com.dgsw.bilimapi.domain.rental.exception.BookAlreadyRentedOutException;
import com.dgsw.bilimapi.domain.rental.exception.NotFriendsException;
import com.dgsw.bilimapi.domain.rental.exception.NotRentalParticipantException;
import com.dgsw.bilimapi.domain.rental.exception.RentalNotFoundException;
import com.dgsw.bilimapi.domain.rental.repository.BookRentalRepository;
import com.dgsw.bilimapi.domain.rental.repository.RentalExtensionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final BookRentalRepository rentalRepository;
    private final RentalExtensionRepository extensionRepository;
    private final UserBookRepository userBookRepository;
    private final FriendshipRepository friendshipRepository;

    @Transactional
    public RentalResponse requestRental(Long borrowerId, Long userBookId) {
        UserBook userBook = userBookRepository.findById(userBookId)
                .orElseThrow(BookNotFoundException::new);

        if (!userBook.isPublic()) {
            throw new NotRentalParticipantException();
        }

        if (userBook.isRentedOut()) {
            throw new BookAlreadyRentedOutException();
        }

        Long lenderId = userBook.getOwnerId();
        friendshipRepository.findAcceptedFriendship(borrowerId, lenderId)
                .orElseThrow(NotFriendsException::new);

        boolean hasPending = !rentalRepository
                .findByUserBookIdAndStatus(userBookId, RentalStatus.PENDING).isEmpty();
        if (hasPending) {
            throw new BookAlreadyRentedOutException();
        }

        BookRental rental = rentalRepository.save(BookRental.builder()
                .userBookId(userBookId)
                .borrowerId(borrowerId)
                .lenderId(lenderId)
                .status(RentalStatus.PENDING)
                .build());

        return RentalResponse.from(rental);
    }

    @Transactional
    public void accept(Long userId, Long rentalId, AcceptRentalRequest request) {
        BookRental rental = rentalRepository.findById(rentalId)
                .orElseThrow(RentalNotFoundException::new);

        if (!rental.getLenderId().equals(userId) || rental.getStatus() != RentalStatus.PENDING) {
            throw new NotRentalParticipantException();
        }

        UserBook userBook = userBookRepository.findById(rental.getUserBookId()).orElseThrow();
        userBook.transferTo(rental.getBorrowerId());

        int days = request.days() != null ? request.days() : 7;
        rental.accept(LocalDate.now().plusDays(days));
    }

    @Transactional
    public void reject(Long userId, Long rentalId) {
        BookRental rental = rentalRepository.findById(rentalId)
                .orElseThrow(RentalNotFoundException::new);

        if (!rental.getLenderId().equals(userId) || rental.getStatus() != RentalStatus.PENDING) {
            throw new NotRentalParticipantException();
        }

        rental.reject();
    }

    @Transactional
    public RentalResponse lend(Long lenderId, LendRequest request) {
        UserBook userBook = userBookRepository.findById(request.userBookId())
                .orElseThrow(BookNotFoundException::new);

        if (!userBook.getOwnerId().equals(lenderId)) {
            throw new NotRentalParticipantException();
        }

        if (userBook.isRentedOut()) {
            throw new BookAlreadyRentedOutException();
        }

        friendshipRepository.findAcceptedFriendship(lenderId, request.friendId())
                .orElseThrow(NotFriendsException::new);

        int days = request.days() > 0 ? request.days() : 7;
        userBook.transferTo(request.friendId());

        BookRental rental = rentalRepository.save(BookRental.builder()
                .userBookId(request.userBookId())
                .borrowerId(request.friendId())
                .lenderId(lenderId)
                .status(RentalStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .dueDate(LocalDate.now().plusDays(days))
                .build());

        return RentalResponse.from(rental);
    }

    @Transactional
    public void returnBook(Long userId, Long rentalId) {
        BookRental rental = rentalRepository.findById(rentalId)
                .orElseThrow(RentalNotFoundException::new);

        if (!rental.getBorrowerId().equals(userId) || rental.getStatus() != RentalStatus.ACTIVE) {
            throw new NotRentalParticipantException();
        }

        UserBook userBook = userBookRepository.findById(rental.getUserBookId()).orElseThrow();
        userBook.returnToOwner();
        rental.returnBook();
    }

    @Transactional
    public void requestExtension(Long userId, Long rentalId, ExtendRequest request) {
        BookRental rental = rentalRepository.findById(rentalId)
                .orElseThrow(RentalNotFoundException::new);

        if (!rental.getBorrowerId().equals(userId) || rental.getStatus() != RentalStatus.ACTIVE) {
            throw new NotRentalParticipantException();
        }

        extensionRepository.save(RentalExtension.builder()
                .rentalId(rentalId)
                .requestedDueDate(request.newDueDate())
                .status(ExtensionStatus.PENDING)
                .build());
    }

    @Transactional
    public void acceptExtension(Long userId, Long rentalId) {
        BookRental rental = rentalRepository.findById(rentalId)
                .orElseThrow(RentalNotFoundException::new);

        if (!rental.getLenderId().equals(userId)) {
            throw new NotRentalParticipantException();
        }

        RentalExtension extension = extensionRepository
                .findByRentalIdAndStatus(rentalId, ExtensionStatus.PENDING)
                .orElseThrow(RentalNotFoundException::new);

        extension.accept();
        rental.extendDueDate(extension.getRequestedDueDate());
    }

    @Transactional(readOnly = true)
    public List<RentalResponse> getMyRentals(Long userId) {
        return rentalRepository.findActiveByUserId(userId).stream()
                .map(RentalResponse::from)
                .toList();
    }

    @Transactional
    public void processExpiredRentals() {
        List<BookRental> expired = rentalRepository.findExpired(LocalDate.now());
        for (BookRental rental : expired) {
            userBookRepository.findById(rental.getUserBookId())
                    .ifPresent(UserBook::returnToOwner);
            rental.expire();
        }
    }
}
