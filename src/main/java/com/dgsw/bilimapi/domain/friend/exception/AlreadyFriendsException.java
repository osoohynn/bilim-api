package com.dgsw.bilimapi.domain.friend.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class AlreadyFriendsException extends BusinessException {

    public AlreadyFriendsException() {
        super(HttpStatus.CONFLICT, "이미 친구이거나 요청이 존재합니다.");
    }
}
