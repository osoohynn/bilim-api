package com.dgsw.bilimapi.domain.reading.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFriendsException extends BusinessException {

    public NotFriendsException() {
        super(HttpStatus.BAD_REQUEST, "친구 관계가 아닙니다.");
    }
}
