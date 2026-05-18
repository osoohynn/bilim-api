package com.dgsw.bilimapi.domain.reading.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class SessionNotFoundException extends BusinessException {

    public SessionNotFoundException() {
        super(HttpStatus.NOT_FOUND, "세션을 찾을 수 없습니다.");
    }
}
