package com.dgsw.bilimapi.domain.rental.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFriendsException extends BusinessException {

    public NotFriendsException() {
        super(HttpStatus.FORBIDDEN, "친구 관계가 아닙니다.");
    }
}
