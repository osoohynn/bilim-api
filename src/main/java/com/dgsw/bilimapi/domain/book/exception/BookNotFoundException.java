package com.dgsw.bilimapi.domain.book.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class BookNotFoundException extends BusinessException {

    public BookNotFoundException() {
        super(HttpStatus.NOT_FOUND, "책을 찾을 수 없습니다.");
    }
}
