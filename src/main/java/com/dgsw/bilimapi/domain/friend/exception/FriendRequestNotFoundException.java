package com.dgsw.bilimapi.domain.friend.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class FriendRequestNotFoundException extends BusinessException {

    public FriendRequestNotFoundException() {
        super(HttpStatus.NOT_FOUND, "친구 요청을 찾을 수 없습니다.");
    }
}
