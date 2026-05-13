package com.dgsw.bilimapi.domain.book.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class WishlistAlreadyExistsException extends BusinessException {

    public WishlistAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "이미 찜한 책입니다.");
    }
}
