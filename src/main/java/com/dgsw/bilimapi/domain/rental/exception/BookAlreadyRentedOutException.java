package com.dgsw.bilimapi.domain.rental.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class BookAlreadyRentedOutException extends BusinessException {

    public BookAlreadyRentedOutException() {
        super(HttpStatus.CONFLICT, "이미 대여 중인 책입니다.");
    }
}
