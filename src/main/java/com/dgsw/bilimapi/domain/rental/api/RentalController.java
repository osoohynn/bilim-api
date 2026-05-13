package com.dgsw.bilimapi.domain.rental.api;

import com.dgsw.bilimapi.domain.rental.dto.AcceptRentalRequest;
import com.dgsw.bilimapi.domain.rental.dto.ExtendRequest;
import com.dgsw.bilimapi.domain.rental.dto.LendRequest;
import com.dgsw.bilimapi.domain.rental.dto.RentalResponse;
import com.dgsw.bilimapi.domain.rental.service.RentalService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/request/{userBookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse requestRental(@PathVariable Long userBookId) {
        return rentalService.requestRental(userBookId);
    }

    @PostMapping("/{rentalId}/accept")
    public void accept(@PathVariable Long rentalId,
                       @RequestBody @Valid AcceptRentalRequest request) {
        rentalService.accept(rentalId, request);
    }

    @PostMapping("/{rentalId}/reject")
    public void reject(@PathVariable Long rentalId) {
        rentalService.reject(rentalId);
    }

    @PostMapping("/lend")
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse lend(@RequestBody @Valid LendRequest request) {
        return rentalService.lend(request);
    }

    @PostMapping("/{rentalId}/return")
    public void returnBook(@PathVariable Long rentalId) {
        rentalService.returnBook(rentalId);
    }

    @PostMapping("/{rentalId}/extend")
    public void requestExtension(@PathVariable Long rentalId,
                                 @RequestBody @Valid ExtendRequest request) {
        rentalService.requestExtension(rentalId, request);
    }

    @PostMapping("/{rentalId}/extend/accept")
    public void acceptExtension(@PathVariable Long rentalId) {
        rentalService.acceptExtension(rentalId);
    }

    @GetMapping
    public List<RentalResponse> getMyRentals() {
        return rentalService.getMyRentals();
    }
}
