package com.dgsw.bilimapi.domain.book.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicateIsbnException extends BusinessException {

    public DuplicateIsbnException() {
        super(HttpStatus.CONFLICT, "이미 등록된 ISBN입니다.");
    }
}
