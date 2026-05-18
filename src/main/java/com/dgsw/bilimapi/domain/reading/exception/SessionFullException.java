package com.dgsw.bilimapi.domain.reading.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class SessionFullException extends BusinessException {

    public SessionFullException() {
        super(HttpStatus.CONFLICT, "세션이 가득 찼습니다. (최대 4인)");
    }
}
