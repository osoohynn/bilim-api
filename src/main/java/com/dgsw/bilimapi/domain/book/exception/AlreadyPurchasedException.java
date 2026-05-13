package com.dgsw.bilimapi.domain.book.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class AlreadyPurchasedException extends BusinessException {

    public AlreadyPurchasedException() {
        super(HttpStatus.CONFLICT, "이미 구입한 책입니다.");
    }
}
