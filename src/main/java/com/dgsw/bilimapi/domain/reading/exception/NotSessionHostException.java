package com.dgsw.bilimapi.domain.reading.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotSessionHostException extends BusinessException {

    public NotSessionHostException() {
        super(HttpStatus.FORBIDDEN, "세션 호스트가 아닙니다.");
    }
}
