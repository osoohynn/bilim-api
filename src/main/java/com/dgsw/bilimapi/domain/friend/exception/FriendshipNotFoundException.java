package com.dgsw.bilimapi.domain.friend.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class FriendshipNotFoundException extends BusinessException {

    public FriendshipNotFoundException() {
        super(HttpStatus.NOT_FOUND, "친구 관계를 찾을 수 없습니다.");
    }
}
