package com.dgsw.bilimapi.domain.book.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotBookOwnerException extends BusinessException {

    public NotBookOwnerException() {
        super(HttpStatus.FORBIDDEN, "해당 책의 소유자가 아닙니다.");
    }
}
