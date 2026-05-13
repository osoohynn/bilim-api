package com.dgsw.bilimapi.domain.rental.api;

import com.dgsw.bilimapi.domain.rental.dto.AcceptRentalRequest;
import com.dgsw.bilimapi.domain.rental.dto.ExtendRequest;
import com.dgsw.bilimapi.domain.rental.dto.LendRequest;
import com.dgsw.bilimapi.domain.rental.dto.RentalResponse;
import com.dgsw.bilimapi.domain.rental.service.RentalService;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final UserRepository userRepository;

    @PostMapping("/request/{userBookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse requestRental(@PathVariable Long userBookId,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        return rentalService.requestRental(getCurrentUserId(userDetails), userBookId);
    }

    @PostMapping("/{rentalId}/accept")
    public void accept(@PathVariable Long rentalId,
                       @RequestBody @Valid AcceptRentalRequest request,
                       @AuthenticationPrincipal UserDetails userDetails) {
        rentalService.accept(getCurrentUserId(userDetails), rentalId, request);
    }

    @PostMapping("/{rentalId}/reject")
    public void reject(@PathVariable Long rentalId,
                       @AuthenticationPrincipal UserDetails userDetails) {
        rentalService.reject(getCurrentUserId(userDetails), rentalId);
    }

    @PostMapping("/lend")
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse lend(@RequestBody @Valid LendRequest request,
                               @AuthenticationPrincipal UserDetails userDetails) {
        return rentalService.lend(getCurrentUserId(userDetails), request);
    }

    @PostMapping("/{rentalId}/return")
    public void returnBook(@PathVariable Long rentalId,
                           @AuthenticationPrincipal UserDetails userDetails) {
        rentalService.returnBook(getCurrentUserId(userDetails), rentalId);
    }

    @PostMapping("/{rentalId}/extend")
    public void requestExtension(@PathVariable Long rentalId,
                                 @RequestBody @Valid ExtendRequest request,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        rentalService.requestExtension(getCurrentUserId(userDetails), rentalId, request);
    }

    @PostMapping("/{rentalId}/extend/accept")
    public void acceptExtension(@PathVariable Long rentalId,
                                @AuthenticationPrincipal UserDetails userDetails) {
        rentalService.acceptExtension(getCurrentUserId(userDetails), rentalId);
    }

    @GetMapping
    public List<RentalResponse> getMyRentals(@AuthenticationPrincipal UserDetails userDetails) {
        return rentalService.getMyRentals(getCurrentUserId(userDetails));
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        UserEntity user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return user.getId();
    }
}
